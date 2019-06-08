package org.programmatori.domotica.own.engine.scsgate;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.joou.UByte;
import org.joou.Unsigned;
import org.programmatori.domotica.own.engine.util.ArrayUtils;
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
public class InputReceiver extends Observable implements SerialPortDataListener, Runnable {
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
		logger.trace("EventType: {}", event.getEventType());

		if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
			byte[] newData = event.getReceivedData(); // I's always null.
			if (newData != null) logger.warn("event.getReceivedData() have data");

			// Work around for getReceivedData()
			SerialPort currentSerial = event.getSerialPort();
			if (currentSerial.bytesAvailable() <= 0) {
				logger.error("Event call without data");
			}

			// Retrieve bytes from bus
			newData = new byte[currentSerial.bytesAvailable()];
			currentSerial.readBytes(newData, newData.length);

			// load queue (It's for security that I transfer the bytes to a queue)
			for (byte newDatum : newData) {
				UByte b = Unsigned.ubyte(newDatum);
				charsQueue.add(b);
			}
			UByte[] logQueue = new UByte[charsQueue.size()];
			logQueue = charsQueue.toArray(logQueue);
			List<UByte> logList = Arrays.asList(logQueue);
			logger.debug("I red: {}", ArrayUtils.bytesToHex(logList));

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

		while (1 == 1) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Stub !!!
			}

			UByte[] logQueue = new UByte[charsQueue.size()];
			logQueue = charsQueue.toArray(logQueue);
			List<UByte> logList = Arrays.asList(logQueue);
			if (logList.size() > 0) {
				logger.debug("Queue: ({}) [{}] - Observer: {}", charsQueue.size(), ArrayUtils.bytesToHex(logList), countObservers());
			} else {
				logger.debug("Queue: (0) [] - Observer: {}", countObservers());
			}

			if (!charsQueue.isEmpty()) {
				setChanged();
				notifyObservers();
				logger.debug("Event send");
			}
		}

	}

	public int count() {
		return charsQueue.size();
	}
}
