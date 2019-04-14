package org.programmatori.domotica.own.engine.row;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.joou.UByte;
import org.joou.Unsigned;
import org.programmatori.domotica.own.engine.util.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

/**
 * Receive Inpot From SGSGate and from BUS
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.1.0, 4/01/2019
 */
public class InputReceiver implements SerialPortDataListener, Runnable {
	private static final Logger logger = LoggerFactory.getLogger(InputReceiver.class);

	private BlockingQueue<UByte> charsQueue = new LinkedBlockingQueue<>();

	public InputReceiver() {
		Thread t = new Thread(this);
		t.setName("SGSGate Queue");
		t.start();
	}

	@Override
	public int getListeningEvents() {
		logger.trace("Call getListeningEvents()");
		return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		logger.debug("EventType: {}", event.getEventType());
		if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {

			byte[] newData = event.getReceivedData(); // I's always null.

			// Work around
			SerialPort currentSerial = event.getSerialPort();
			byte[] newData2 = new byte[currentSerial.bytesAvailable()];
			//int numRead =
			currentSerial.readBytes(newData2, newData2.length);
			newData = newData2;

			if (newData != null) {
				// load queue
				for (int i = 0; i < newData.length; ++i) {
					UByte b = Unsigned.ubyte(newData[i]);
					charsQueue.add(b);
				}
			}

		} else if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN) {
			logger.warn("Event Data Written");
		}
	}

	public String takeString(int length) {
		String value = "";

		for (int i = 0; i < length; i++) {
			UByte b = take();
			if (b == null) return null;

			char ch = (char) b.byteValue();
			value += ch;
		}

		logger.debug("get from Queue: {}", value);
		return value;
	}

	public UByte take() {
		try {
			return charsQueue.take();
		} catch (InterruptedException e) {
			logger.error("Interruption of the waiting of the value", e);
			return null;
		}
	}

	public UByte[] take(int length) {
		UByte values[] = new UByte[length];
		for (int i = 0; i < length; i++) {
			values[i] = take();
		}

		return values;
	}

	public char takeChar() {
		return (char) take().byteValue();
	}

	@Override
	public void run() {

		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// Stub !!!
			}

			UByte[] logQueue = new UByte[charsQueue.size()];
			logQueue = charsQueue.toArray(logQueue);
			List<UByte> logList = Arrays.asList(logQueue);
			if (logList.size() > 0) {
				logger.debug("Queue: ({}) {}", charsQueue.size(), ArrayUtils.bytesToHex(logList));
			} else {
				logger.debug("Queue: (0) []");
			}
		}

	}
}
