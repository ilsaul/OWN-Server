/*
 * Copyright (C) 2010-2019 Moreno Cattaneo <moreno.cattaneo@gmail.com>
 *
 * This file is part of OWN Server.
 *
 * OWN Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 * OWN Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with OWN Server.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.programmatori.domotica.own.engine.scsgate;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.joou.UByte;
import org.joou.Unsigned;
import org.programmatori.domotica.own.engine.util.ArrayUtils;
import org.programmatori.domotica.own.sdk.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Receive Input From SGSGate that receive input from BUS
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.1.0, 4/01/2019
 */
public class InputReceiver extends Observable implements SerialPortDataListener {
	private static final Logger logger = LoggerFactory.getLogger(InputReceiver.class);

	private BlockingQueue<UByte> charsQueue = new LinkedBlockingQueue<>();
	private SerialPort serial;

	public InputReceiver(SerialPort serial) {
		this.serial = serial;
	}

	@Override
	public int getListeningEvents() {
		logger.trace("Call getListeningEvents()");

		return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		logger.trace("EventType: {}", event.getEventType());

		if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {

			// Retrieve bytes from bus
			byte[] newData = event.getReceivedData(); // I's always null.
			if (newData == null) {

				// Work around for getReceivedData()
				SerialPort currentSerial = event.getSerialPort();
				if (currentSerial.bytesAvailable() <= 0) {
					logger.error("Event call without data");
				}

				newData = new byte[currentSerial.bytesAvailable()];
				currentSerial.readBytes(newData, newData.length);
			}

			// load queue (It's for security that I transfer the bytes to a queue)
			for (byte newDatum : newData) {
				UByte b = Unsigned.ubyte(newDatum);
				charsQueue.add(b);
			}
			UByte[] logQueue = new UByte[charsQueue.size()];
			logQueue = charsQueue.toArray(logQueue);
			List<UByte> logList = Arrays.asList(logQueue);
			String msg = ArrayUtils.bytesToHex(logList);
			logger.debug("I red from SCSGate: {}", msg);

			if (!charsQueue.isEmpty()) {
				setChanged();
				notifyObservers();
				logger.debug("Event send");
			}

		} else if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN) {
			logger.warn("Event Data Written");
		}

		logQueue();
	}

	public String takeString(int length) {
		StringBuilder value = new StringBuilder();

		for (int i = 0; i < length; i++) {
			UByte b = take();
			if (b == null) return null;

			char ch = (char) b.byteValue();
			value.append(ch);
		}

		logger.debug("get from Queue: {}", value);
		return value.toString();
	}

	/**
	 * Remove the value
	 * @return
	 */
	public UByte take() {
		try {
			return charsQueue.take();
		} catch (InterruptedException e) {
			logger.error("Interruption of the waiting of the value", e);
			return null;
		}
	}

	/**
	 * Watch the value but don't remove.
	 * @return
	 */
	public UByte peek() {
		return charsQueue.peek();
	}

	public UByte[] take(int length) {
		UByte[] values = new UByte[length];
		for (int i = 0; i < length; i++) {
			values[i] = take();
		}

		return values;
	}

	public char takeChar() {
		return (char) take().byteValue();
	}

	private void logQueue() {
		UByte[] logQueue = new UByte[charsQueue.size()];
		logQueue = charsQueue.toArray(logQueue);
		List<UByte> logList = Arrays.asList(logQueue);
		if (!logList.isEmpty()) {
			String list = ArrayUtils.bytesToHex(logList);
			logger.debug("Queue: ({}) [{}] - Ob: {}", charsQueue.size(), list, countObservers());
		} else {
			logger.debug("Queue: (0) [] - Ob: {} - St: {} r/w: {}/{}", countObservers(), serial.isOpen(), serial.bytesAvailable(), serial.bytesAwaitingWrite());
		}
	}

	public int count() {
		return charsQueue.size();
	}
}
