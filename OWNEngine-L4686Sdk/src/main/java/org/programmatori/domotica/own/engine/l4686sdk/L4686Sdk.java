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

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.SCSEvent;
import org.programmatori.domotica.own.sdk.server.engine.SCSListener;
import org.programmatori.domotica.own.sdk.server.engine.Serial;
import org.programmatori.domotica.own.sdk.server.engine.core.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.TooManyListenersException;

/**
 * This Engine use the BTicino component L4686SDK to talk with the bus scs.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.3.0, 23/05/2019
 */
public class L4686Sdk extends Serial implements Engine, Observer {
	private static final Logger logger = LoggerFactory.getLogger(L4686Sdk.class);

	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;

	private OutputStream out;
	private List<SCSListener> listListener;

	private InputReceiver input;

	public L4686Sdk() {
		out = null;
		listListener = new ArrayList<>();

		findSerial(Config.getInstance().getNode("l4686sdk"));
		// I leave default setting

		logger.info("L4686Sdk Initialized");
	}

	@Override
	public void start() throws IOException {
		connect();
	}

	/**
	 * This should be called when you stop using the port. This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		super.close();
	}

	@Override
	public boolean isReady() {
		return isConnected();
	}

	@Override
	protected boolean connect() throws IOException {
		if (super.connect()) {
			input = new InputReceiver(getSerial());
			//in = getSerial().getInputStream();
			//getSerial().addDataListener(this);
			getSerial().addDataListener(input);
			out = getSerial().getOutputStream();

			return busInitialize();
		} else {
			logger.info("Not Connected");
		}

		return false;

//		//TODO: Manage connected COM
////		if (portIdentifier.isCurrentlyOwned()) {
////			throw new PortInUseException();
	}

	private boolean busInitialize() {
		// usefull?
		return true;
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
	public void addEventListener(SCSListener listener) throws TooManyListenersException {
		listListener.add(listener);
	}

	@Override
	public void removeEventListener(SCSListener listener) {
		listListener.remove(listener);
	}

	private void notifyListeners(SCSEvent event) {
		for (SCSListener listener: listListener)
			listener.scsValueChanged(event);
	}

//	@Override
//	public synchronized void serialEvent(SerialPortEvent event) {
//		logger.trace("Event: {}", event);
//		if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
//
//			int available = 0;
//			String data = "";
//
//			// Work around
//			SerialPort currentSerial = event.getSerialPort();
//			available = currentSerial.bytesAvailable();
//
//			byte[] buffer = new byte[available];
//			logger.debug("available: {} buffer.length: {}", available, buffer.length);
//			currentSerial.readBytes(buffer, buffer.length);
//			data = new String(buffer, 0, buffer.length);
//			logger.debug("Serial data: {}", data);
//
//			// Bug.id 8: When 2 msg is in the same line
//			if (data.length() > 0) {
//				dirtyBuffer += data;
//			}
//
//			// Continue until there is message
//			while (dirtyBuffer.contains(SCSMsg.MSG_ENDED)) {
//				int pos = dirtyBuffer.indexOf(SCSMsg.MSG_ENDED);
//				if (pos > 0) {
//					String cmd = dirtyBuffer.substring(0, pos+2);
//					dirtyBuffer = dirtyBuffer.substring(pos+2);
//
//					logger.debug("RX from BUS: {}", cmd);
//					SCSEvent scsEvent = new SCSEvent(this, cmd);
//					notifyListeners(scsEvent);
//				}
//
//				// I leave this part because I need to test better if I can receive dirty data.
////				else {
////					// Here it mean i don't understand what can i do with this date
////					dirtyBuffer = data;
////				}
//			}
//
//		} else {
//			logger.debug("Event occurred: {}", event.getEventType());
//		}
//
//		logger.trace("End serialEvent");
//	}

	/**
	 * Observable Object is only One that no need to check if it is the true object.
	 *
	 * @param o Object that I observe
	 * @param arg Value Observed
	 */
	@Override
	public void update(Observable o, Object arg) {
		retrieveMessage();
	}

	private void retrieveMessage() {
		do {
			SCSMsg msg = null;
			String row = null;

			try {
				row = input.take();
				msg = new SCSMsg(row);
			} catch (Exception e) {
				logger.error("Error in conversion", e);
			}

			logger.debug("Row: {} -> SCS: {}", row, msg);

			if (msg != null) Config.getInstance().getMessageLog().log(msg.toString());

			SCSEvent event = new SCSEvent(this, msg);
			notifyListeners(event);
		} while (input.count() != 0);
	}

	/**
	 * Test porpose
	 */
	public static void main(String[] args) {
//		try {
//			(new L4686Sdk()).connect("COM6");
//			//(new L4686Sdk()).connect("cu.usbserial-1410");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}
