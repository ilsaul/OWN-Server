/*
 * OWN Server is
 * Copyright (C) 2010-2015 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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

import gnu.io.*;

import java.io.*;
import java.util.*;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.SCSEvent;
import org.programmatori.domotica.own.sdk.server.engine.SCSListener;
import org.programmatori.domotica.own.sdk.server.engine.core.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Engine use the BTicino component L4686SDK to talk with the bus scs.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.2.2, 13/01/2015
 */
public class L4686Sdk implements Engine, SerialPortEventListener {
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

	public L4686Sdk() throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException, IOException, TooManyListenersException {
		serialPort = null;
		in = null;
		out = null;
		dirtyBuffer = null;

		listListener = new ArrayList<SCSListener>();
		connect();
	}

	public void connect() throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException, IOException, TooManyListenersException {
		connect(Config.getInstance().getNode("l4686sdk"));
	}

	/**
	 * This should be called when you stop using the port. This will prevent port locking on platforms like Linux.
	 */
	public synchronized void disconnect() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	private void connect(String portName) throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException, IOException, TooManyListenersException {
		CommPortIdentifier portIdentifier = null;

		try  {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		} catch (NoSuchPortException e) {
			String msg = "Port Not Found! Availble ports:";
			Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
			while (portEnum.hasMoreElements()) {
				CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
				msg += " " + currPortId.getName();
			}
			logger.warn(msg);

			throw e;
		}

		if (portIdentifier.isCurrentlyOwned()) {
			throw new PortInUseException();
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(), TIME_OUT);

			if (commPort instanceof SerialPort) {
				serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				logger.info("L4686Sdk Connected");

				in = serialPort.getInputStream();
				out = serialPort.getOutputStream();

				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);

			} else {
				logger.error("Only serial ports are manage.");
			}
		}
	}

//	public OutputStream getOut() {
//		return out;
//	}

	@Override
	public void addEventListener(SCSListener listener) throws TooManyListenersException {
		listListener.add(listener);
	}

	@Override
	public void removeEventListener(SCSListener listener) {
		listListener.remove(listener);
	}

	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

			int available = 1;
			String data = "";

			// Iterate until i have date
			while (available > 0) { // || (dirtyBuffer != null && dirtyBuffer.length() > 0)) {
				try {
					available = in.available();
					data = "";

					if (available > 0) {
						byte buffer[] = new byte[available];
						in.read(buffer, 0, available);

						logger.debug("available: {} buffer.length: {}", available, buffer.length);
						data = new String(buffer, 0, buffer.length);
						logger.debug("Serial data: {}", data);
					}

					// Add dirty information to the data
					if (dirtyBuffer != null && dirtyBuffer.length() > 0) {
						data = dirtyBuffer + data;
						dirtyBuffer = null;
					}

					// Bug.id 8: When 2 msg is in the same line
					if (data.length() > 0) {
						int pos = data.indexOf(SCSMsg.MSG_ENDER);
						if (pos > 0) {
							String cmd = data.substring(0, pos+2);
							dirtyBuffer = data.substring(pos+2, data.length());

							logger.debug("RX from BUS: {}", cmd);
							SCSEvent event = new SCSEvent(this, cmd);
							notifyListeners(event);
						} else {
							// Here it mean i don't understand what can i do with this date
							dirtyBuffer = data;
						}
					}

				} catch (IOException e) {
					logger.error("Error:", e);
					System.exit(-1);
				}
			}

			// There is something on the buffer but i don't understand what is
			if (dirtyBuffer != null && dirtyBuffer.length() > 0) {
				logger.warn("Dirty Message: {}", dirtyBuffer);
			}

		} else {
			logger.debug("Event occured: {}", oEvent.getEventType());
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
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	public static void main(String[] args) {
		try {
			(new L4686Sdk()).connect("COM6");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
