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
package org.programmatori.domotica.own.engine.l4686sdk;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Receive Input From L4686SDK
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.1.0, 20/07/2019
 */
public class InputReceiver extends Observable implements SerialPortDataListener {
	private static final Logger logger = LoggerFactory.getLogger(InputReceiver.class);

	private final BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>();
	private final SerialPort serial;
	private String dirtyBuffer;

	public InputReceiver(SerialPort serial) {
		this.serial = serial;
		dirtyBuffer = "";
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

			String data = "";

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
			data = new String(newData, 0, newData.length);
			logger.debug("I red from L4686SDK: {}", data);

			// Bug.id 8: When 2 msg is in the same line
			if (data.length() > 0) {
				dirtyBuffer += data;
			}

			// Continue until there is message
			while (dirtyBuffer.contains(SCSMsg.MSG_ENDED)) {
				int pos = dirtyBuffer.indexOf(SCSMsg.MSG_ENDED);
				if (pos > 0) {
					String cmd = dirtyBuffer.substring(0, pos+2);
					dirtyBuffer = dirtyBuffer.substring(pos+2);

					logger.debug("RX from BUS: {}", cmd);
					msgQueue.add(cmd);

					if (!msgQueue.isEmpty()) {
						setChanged();
						notifyObservers();
						logger.debug("Event send");
					}
				}
			}

		} else if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN) {
			logger.warn("Event Data Written");
		}

		logQueue();
	}

	/**
	 * Remove the value
	 * @return
	 */
	public String take() {
		try {
			return msgQueue.take();
		} catch (InterruptedException e) {
			logger.error("Interruption of the waiting of the value", e);
			return null;
		}
	}

	/**
	 * Watch the value but don't remove.
	 * @return
	 */
	public String peek() {
		return msgQueue.peek();
	}

	private void logQueue() {
		if (!msgQueue.isEmpty()) {
			logger.debug("Queue: ({}) [{}] - Ob: {}", msgQueue.size(), msgQueue, countObservers());
		} else {
			logger.debug("Queue: (0) [] - Ob: {} - St: {} r/w: {}/{}", countObservers(), serial.isOpen(), serial.bytesAvailable(), serial.bytesAwaitingWrite());
		}
	}

	public int count() {
		return msgQueue.size();
	}
}
