/*
 * OWN Server is
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
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.SCSEvent;
import org.programmatori.domotica.own.sdk.server.engine.SCSListener;
import org.programmatori.domotica.own.sdk.server.engine.core.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

/**
 * This Engine use the BTicino component L4686SDK to talk with the bus scs.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.3.0, 23/05/2019
 */
public class L4686Sdk implements Engine, SerialPortDataListener {
	private static final Logger logger = LoggerFactory.getLogger(L4686Sdk.class);

	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 115200;

	private SerialPort serialPort;
	private InputStream in;
	private OutputStream out;
	private List<SCSListener> listListener;
	private String dirtyBuffer;

	public L4686Sdk() throws IOException {
		serialPort = null;
		in = null;
		out = null;
		dirtyBuffer = "";

		listListener = new ArrayList<SCSListener>();
		logger.info("L4686Sdk Initialized");
		connect(); //TODO: Remove from constructor
	}

	public void connect() throws IOException {
		connect(Config.getInstance().getNode("l4686sdk"));
	}

	/**
	 * This should be called when you stop using the port. This will prevent port locking on platforms like Linux.
	 */
	public synchronized void disconnect() {
		if (serialPort != null) {
			serialPort.removeDataListener();
			serialPort.closePort();
		}
	}

	private void connect(String portName) throws IOException {

		if (portName == null || portName.trim().length() == 0) {
			logger.error("Port Name missing.");
			return;
		}

		//serialPort = SerialPort.getCommPort(portName);

		serialPort = getSerialPort(portName);
		if (serialPort == null) {
			throw new IOException("Serial Port not found '" + portName + "'");
		}

		//TODO: Manage connected COM
//		if (portIdentifier.isCurrentlyOwned()) {
//			throw new PortInUseException();
//		} else {

		serialPort.setBaudRate(DATA_RATE);
		serialPort.setNumDataBits(8);
		serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
		serialPort.setParity(SerialPort.NO_PARITY);
		serialPort.openPort();

		if (serialPort.isOpen()) {
			logger.info("L4686Sdk Connected to {} ({})", serialPort.getSystemPortName(), serialPort.getPortDescription());

			in = serialPort.getInputStream();
			out = serialPort.getOutputStream();

			serialPort.addDataListener(this);
		} else {
			StringBuilder msg  = new StringBuilder();
			msg.append("Port Not Found! Available ports:");


			boolean first = true;
			SerialPort[] ports = SerialPort.getCommPorts();
			for (SerialPort tempPort: ports) {
				if (first) {
					first = false;
				} else {
					msg.append(",");
				}
				msg.append(" ");
				msg.append(tempPort.getDescriptivePortName());
			}
			logger.warn(msg.toString());
		}

//			if (commPort instanceof SerialPort) {
//				serialPort = (SerialPort) commPort;
//				serialPort.setSerialPortParams(, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
//				logger.info("L4686Sdk Connected");
//
//				in = serialPort.getInputStream();
//				out = serialPort.getOutputStream();
//
//				serialPort.addEventListener(this);
//				serialPort.addDataListener(this);
//				serialPort.notifyOnDataAvailable(true);
//
//			} else {
//				logger.error("Only serial ports are manage.");
//			}
//		}
	}

	private SerialPort getSerialPort(String portName) {
		SerialPort[] ports = SerialPort.getCommPorts();
		for (int i = 0; i < ports.length; i++) {
			SerialPort currentSerial = ports[i];

			logger.debug("Port: {}->{}", currentSerial.getSystemPortName(), currentSerial.getDescriptivePortName());

			// I can't find a better system than use a name
			if (currentSerial.getDescriptivePortName().equals(portName)) {
				logger.info("Port found {}", currentSerial.getSystemPortName());
				return currentSerial;
			}
		}

		return null;
	}

//	public OutputStream getOut() {
//		return out;
//	}

	/**
	 * What I want to receive
	 *
	 * @return
	 */
	public int getListeningEvents() {
		logger.trace("Call getListeningEvents()");
		return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
	}

	@Override
	public void addEventListener(SCSListener listener) throws TooManyListenersException {
		listListener.add(listener);
	}

	@Override
	public void removeEventListener(SCSListener listener) {
		listListener.remove(listener);
	}


	@Override
	public synchronized void serialEvent(SerialPortEvent event) {
		logger.trace("Event: {}", event);
		if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {

			int available = 0;
			String data = "";

			// Work around
			SerialPort currentSerial = event.getSerialPort();
			available = currentSerial.bytesAvailable();

			byte[] buffer = new byte[available];
			logger.debug("available: {} buffer.length: {}", available, buffer.length);
			currentSerial.readBytes(buffer, buffer.length);
			data = new String(buffer, 0, buffer.length);
			logger.debug("Serial data: {}", data);

			// Bug.id 8: When 2 msg is in the same line
			if (data.length() > 0) {
				dirtyBuffer += data;
			}

			// Continue until there is message
			while (dirtyBuffer.contains(SCSMsg.MSG_ENDER)) {
				int pos = dirtyBuffer.indexOf(SCSMsg.MSG_ENDER);
				if (pos > 0) {
					String cmd = dirtyBuffer.substring(0, pos+2);
					dirtyBuffer = dirtyBuffer.substring(pos+2);

					logger.debug("RX from BUS: {}", cmd);
					SCSEvent scsEvent = new SCSEvent(this, cmd);
					notifyListeners(scsEvent);
				}

				// I leave this part because I need to test better if I can receive dirty data.
//				else {
//					// Here it mean i don't understand what can i do with this date
//					dirtyBuffer = data;
//				}
			}

		} else {
			logger.debug("Event occurred: {}", event.getEventType());
		}

		logger.trace("End serialEvent");
	}

	private void notifyListeners(SCSEvent event) {
		for (SCSListener listener: listListener)
			listener.SCSValueChanged(event);
	}

	@Override
	public synchronized void sendCommand(SCSMsg msg) {
		try {
			logger.debug("TX to BUS: {}", msg.toString());
			out.write(msg.toString().getBytes());
		} catch (IOException e) {
			logger.error("Error:", e);
		}
	}

	@Override
	public boolean isReady() {
		return true;
	}

	/**
	 * This should be called when you stop using the port. This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeDataListener();
			serialPort.closePort();
		}
	}

	/**
	 * Test porpose
	 */
	public static void main(String[] args) {
		try {
			(new L4686Sdk()).connect("COM6");
			//(new L4686Sdk()).connect("cu.usbserial-1410");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
