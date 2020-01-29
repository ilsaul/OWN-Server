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

import org.joou.UByte;
import org.joou.Unsigned;
import org.programmatori.domotica.own.engine.util.ArrayUtils;
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
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.TooManyListenersException;

/**
 * This Engine use a SGSGate make from Guido Pic for connect OWN Server with the real SCS Bus. I don't sell any hardware
 * for use with this software because I made for my own use.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.1.0, 05/06/2018
 */
public class SCSGate extends Serial implements Engine, Observer {
	private static final Logger logger = LoggerFactory.getLogger(SCSGate.class);

	//private static final String WELCOME = "Started!\r\n";
	private static final String WELCOME = "Serial v2.0\r\n";
	private static final String START_SEND = "@W"; // 0x40 0x57
	private static final String SCSGATE_VOLT = "@5";
	private static final String SCSGATE_BYTE = "@MX";
	private static final String SCSGATE_READ = "@l";
	private static final String SCSGATE_VERSION = "@q";

	private static final int MAX_WRITE_SGSGATE = 15;

	private SCSGateState state;
	private OutputStream out;
	private List<SCSListener> listListener;

	private InputReceiver input;

	public SCSGate() {
		logger.trace("Trace Enable");
		logger.debug("Debug Enable");
		logger.info("Info Enable");
		findSerial(Config.getInstance().getNode("scsgate"));
		// I leave default setting

		listListener = new ArrayList<>();
		state = SCSGateState.STATE_INITIAL;
	}

	@Override
	public void start() throws IOException {
		connect();
	}

	@Override
	public void close() {
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
			getSerial().addDataListener(input);
			out = getSerial().getOutputStream();

			return busInitialize();
		} else {
			logger.info("Not Connected");
		}

		return false;
	}

	private boolean busInitialize() {
		logger.trace("Starting busInitialize");

		String errorMsg = "Error to {}";
		String setMsg = "Settled {}";

		// Check state
		if (state == SCSGateState.STATE_INITIAL) {
			busInitializeReady(errorMsg, setMsg);
			sendSGSGateCommand(SCSGATE_VOLT);
		}

		if (state == SCSGateState.STATE_ARDUINO_READY) {
			busInitializeVolt(errorMsg, setMsg);
			sendSGSGateCommand(SCSGATE_BYTE);
		}

		if (state == SCSGateState.STATE_SCS_GATE_SET_VOLT) {
			busInitializeMode(errorMsg, setMsg);
			sendSGSGateCommand(SCSGATE_READ);
		}

		if (state == SCSGateState.STATE_SCS_GATE_SET_MODE) {
			busInitializeLog(errorMsg, setMsg);
			sendSGSGateCommand(SCSGATE_VERSION);
		}

		if (state == SCSGateState.STATE_SCS_GATE_SET_LOG) {
			busInitializeVersion();
		}

		logger.debug("Add Serial Observer");
		input.addObserver(this);
		return state == SCSGateState.STATE_SCS_GATE_READY;
	}

	private void busInitializeReady(String errorMsg, String setMsg) {
		logger.trace("Start busInitializeInitial");
		StringBuilder value = new StringBuilder();

		while (!WELCOME.equals(value.toString())) {
			value.append(input.takeChar());
			logger.debug("value: {} - queue: {}", value, input.count());

			if (!WELCOME.startsWith(value.toString())) {
				logger.trace("Wrong Message I delete");
				while (!WELCOME.startsWith(value.toString()) && value.length() > 0) {
					String delete = value.substring(0,1);
					logger.debug("Delete byte '{}' - value size: {}", delete, value.length());
					value.delete(0, 1);
				}
				logger.trace("Wrong Message after delete");
			}
		}

		if (WELCOME.equals(value.toString())) {
			state = state.next();
			logger.info(state.getDescription());
		} else {
			logger.error(errorMsg, state.next().getDescription());
		}
	}

	private void busInitializeVolt(String errorMsg, String setMsg) {
		logger.trace("start busInitializeVolt");
		char ch = input.takeChar();

		if (ch == 'k') {
			state = state.next();
			logger.info(setMsg, state.getDescription());
		} else {
			logger.error(errorMsg, state.next().getDescription());
		}
	}

	private void busInitializeMode(String errorMsg, String setMsg) {
		logger.trace("start busInitializeByte");
		char ch = input.takeChar();

		if (ch == 'k') {
			state = state.next(); // I must change always state
			logger.info(setMsg, state.getDescription());
		} else {
			logger.error(errorMsg, state.next().getDescription());
		}
	}

	private void busInitializeLog(String errorMsg, String setMsg) {
		logger.trace("start busInitializeByte");
		char ch = input.takeChar();

		if (ch == 'k') {
			state = state.next(); // I must change always state
			logger.info(setMsg, state.getDescription());
		} else {
			logger.error(errorMsg, state.next().getDescription());
		}
	}

	private void busInitializeVersion() {
		logger.trace("start busInitializeLog");
		char ch = input.takeChar();

		if (ch == 'k') {
			String version = input.takeString(9);

			logger.info("SCS Gate: Version {}", version);
			state = state.next();
		}
	}

	/**
	 * This is use for send @ coomand to SCSGate
	 * @param sendRow
	 */
	public void sendSGSGateCommand(String sendRow) {
		logger.info("Send command to SCSGate: {}", sendRow);
		byte[] byteRow = sendRow.getBytes();
		UByte[] row = new UByte[byteRow.length];

		for (int i = 0; i < byteRow.length; i++) {
			row[i] = Unsigned.ubyte(byteRow[i]);
		}

		sendRow(row);
	}

	public void sendRow(UByte[] sendRow) {
		List<UByte> list = new ArrayList<>();
		list.addAll(Arrays.asList(sendRow));

		String values = ArrayUtils.bytesToHex(list);
		logger.debug("SendRow {}", values);

		try {
			for (UByte b : list) {
				out.write(b.byteValue());
				Thread.sleep(0,10000); // Sleep 1 millisecond
			}

			out.flush();
			Thread.sleep(5);

		} catch (IOException e) {
			logger.error("Error:", e);
		} catch (InterruptedException e) {
			logger.error("Interrupt Send Message:", e);
		}
	}

	private void sendWriteToSGSGate(UByte[] sendRow) {
		int pos = Math.min(MAX_WRITE_SGSGATE, sendRow.length);
		List<UByte> msg = new ArrayList<>();

		for (int i = 0; i < sendRow.length; i++) {
			if (msg.isEmpty()) {
				msg.add(UByte.valueOf(START_SEND.charAt(0)));
				msg.add(UByte.valueOf(START_SEND.charAt(1)));
				msg.add(UByte.valueOf(pos));
			}

			msg.add(sendRow[i]);
			pos--;

			if (pos == 0) {
				sendRow(msg.toArray(new UByte[msg.size()]));
				msg.clear();
				pos = Math.min(MAX_WRITE_SGSGATE, sendRow.length - i);
			}
		}
	}

	@Override
	public void sendCommand(SCSMsg msg) {
		SCSConverter scsConverter = new SCSConverter();
		UByte[] sendRow = scsConverter.convertFromSCS(msg);
		String sRow = ArrayUtils.bytesToString(sendRow);
		logger.debug("Send {} -> {}", msg, sRow);

		sendWriteToSGSGate(sendRow);
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

	/**
	 * Remove from serialEvent for make little method. Search to understand the message
	 * and encode it.
	 */
	private void retrieveMessage() {
		SCSMsg msg = null;
		SCSConverter scsConverter = new SCSConverter();
		int length = 0;

		do {
			List<UByte> rowMsg = new ArrayList<>();

			// Take first character for understand the message
			UByte b = input.take();

			if (b.intValue() > 0 && b.intValue() <= 0x0F) {
				length = b.intValue();
				logger.debug("length {}", length);

				// Read Message
				for (int i = 0; i < length; i++) {
					rowMsg.add(input.take());
				}

			} else if (b.intValue() == 'k') {
				logger.debug("Accepted preview operation");

			} else {
				// Discard unknown byte
				String res = ArrayUtils.byteToHex(b);
				logger.warn("Discard byte {}", res);
			}

			if (!rowMsg.isEmpty()) {
				UByte[] list = new UByte[rowMsg.size()];

				try {
					length = 0;
					msg = scsConverter.convertToSCS(rowMsg.toArray(list));
				} catch (Exception e) {
					logger.error("Error in conversion", e);
				}

				// Log
				String row = ArrayUtils.bytesToHex(rowMsg);
				logger.debug("Row: {} -> SCS: {}", row, msg);

				if (msg != null) Config.getInstance().getMessageLog().log(msg.toString());

				SCSEvent event = new SCSEvent(this, msg);
				notifyListeners(event);
			}

		} while (input.count() != 0);
	}

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

	public static void main(String[] args) throws IOException, InterruptedException {
		SCSGate engine = new SCSGate();
		engine.start();

		Thread.sleep(5000);

		UByte[] sendRow;
		//Deque<UByte> msg = new ArrayDeque<>();

//		//a8:b1:00:15:00:a4:a3
//		for (int y = 0; y < 15; y++) {
//			msg.add(UByte.valueOf(0xB0 + y));
//			msg.add(UByte.valueOf(0x00));
//			msg.add(UByte.valueOf(0x15));
//			msg.add(UByte.valueOf(0x00));
//			UByte[] ret = new UByte[msg.size()];
//			msg.add(SCSConverter.calcHash(msg.toArray(ret)));
//			msg.add(UByte.valueOf(0xA3));
//			msg.push(UByte.valueOf(0xA8));
//
//			ret = new UByte[msg.size()];
//			engine.sendWriteToSGSGate(msg.toArray(ret));
//
//			msg.clear();
//			Thread.sleep(10000);
//		}

		// Power
		//A8 B7 01 13 04 A1 A3
		//sendRow = ArrayUtils.stringToArray("a8:01:CA:15:00:DC:A3");
		//engine.sendWriteToSGSGate(sendRow);

		// Luce Accesa
		//sendRow = ArrayUtils.stringToArray("a8:24:20:12:00:16:a3"); //ON
		sendRow = ArrayUtils.stringToArray("a8:24:ca:12:00:fc:a3");
		//sendRow = ArrayUtils.stringToArray("a8:24:ca:15:00:fb:a3"); // Status
		engine.sendWriteToSGSGate(sendRow);

		Thread.sleep(5000);

		//sendRow = ArrayUtils.stringToArray("a8:24:20:12:01:17:a3"); //OFF non va
		sendRow = ArrayUtils.stringToArray("a8:24:ca:12:01:fd:a3");
		engine.sendWriteToSGSGate(sendRow);

		logger.debug("Bytes wait to write: {}", engine.getSerial().bytesAwaitingWrite());

		Config.getInstance().setExit(true);

//		//row.send("@q");
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		engine.close();
	}
}