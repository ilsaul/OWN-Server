package org.programmatori.domotica.own.sdk.server.engine;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class Serial {
	private static final Logger logger = LoggerFactory.getLogger(Serial.class);

	/** Default bits per second for COM port. */
	private static final int BAUD_RATE = 115200;

	private SerialPort currentSerial;

	protected SerialPort findSerial(String portName) {
		SerialPort[] ports = SerialPort.getCommPorts();

		currentSerial = null;
		for (int i = 0; i < ports.length; i++) {
			SerialPort serialPort = ports[i];

			logger.debug("Port {}->{}", serialPort.getSystemPortName(), serialPort.getDescriptivePortName());

			// I can't find a better system than use a name
			if (serialPort.getDescriptivePortName().equals(portName)) {
				logger.info("Port found {}", serialPort.getSystemPortName());
				currentSerial = serialPort;

				// Setting Work Information for Serial
				currentSerial.setBaudRate(getBaudRate());
				currentSerial.setNumDataBits(getNumDataBits());
				currentSerial.setNumStopBits(getNumStopBits());
				currentSerial.setParity(getParity());
				//currentSerial.setFlowControl();
				//currentSerial.setComPortTimeouts();
			}
		}

		if (currentSerial == null) {
			logger.error("Port not Found {}", portName);
		}

		return currentSerial;
	}

	protected SerialPort getSerial() {
		return currentSerial;
	}

	protected boolean connect() throws IOException {
		if (currentSerial == null) throw new IOException("Port not available");

		boolean connected = currentSerial.openPort();
		logger.info("Serial Connection: {}", connected);
		return connected;
	}

	protected boolean isConnected() {
		return currentSerial.isOpen();
	}

	protected int getBaudRate() {
		return BAUD_RATE;
	}

	protected int getNumDataBits() {
		return 8;
	}

	protected int getNumStopBits() {
		return SerialPort.ONE_STOP_BIT;
	}

	private int getParity() {
		return SerialPort.NO_PARITY;
	}

	protected void close() {
		currentSerial.closePort();
		currentSerial = null;
	}
}
