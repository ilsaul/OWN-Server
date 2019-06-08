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

	private static final String WELCOME = "Started!\r\n";

	private SCSGateState state;
	private OutputStream out;
	private List<SCSListener> listListener;

	private InputReceiver input = new InputReceiver();

	public SCSGate() {
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
			busInitializeInitial(errorMsg);
		}

		if (state == SCSGateState.STATE_ARDUINO_READY) {
			busInitializeReady(errorMsg, setMsg);
		}

		if (state == SCSGateState.STATE_SCS_GATE_SET_SLOW_SPEED) {
			busInitializeSpeed(errorMsg, setMsg);
		}

		if (state == SCSGateState.STATE_SCS_GATE_SET_VOLT) {
			busInitializeVolt(errorMsg, setMsg);
		}

		if (state == SCSGateState.STATE_SCS_GATE_SET_ASCII) {
			busInitializeAscii(errorMsg, setMsg);
		}

		if (state == SCSGateState.STATE_SCS_GATE_SET_LOG) {
			busInitializeLog();
		}

		logger.debug("Add Serial Observer");
		input.addObserver(this);
		return state == SCSGateState.STATE_SCS_GATE_READY;
	}

	private void busInitializeInitial(String errorMsg) {
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
					value.delete(1, 1);
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

	private void busInitializeReady(String errorMsg, String setMsg) {
		logger.trace("start busInitializeReady");
		char ch = input.takeChar();

		if (ch == 'k') {
			state = state.next();
			logger.info(setMsg, state.getDescription());
		} else {
			logger.error(errorMsg, state.next().getDescription());
		}
	}

	private void busInitializeSpeed(String errorMsg, String setMsg) {
		logger.trace("start busInitializeSpeed");
		char ch = input.takeChar();

		if (ch == 'k') {
			state = state.next();
			logger.info(setMsg, state.getDescription());
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

	private void busInitializeAscii(String errorMsg, String setMsg) {
		logger.trace("start busInitializeAscii");
		char ch = input.takeChar();

		if (ch == 'k') {
			state = state.next(); // I must change always state
			logger.info(setMsg, state.getDescription());
		} else {
			logger.error(errorMsg, state.next().getDescription());
		}
	}

	private void busInitializeLog() {
		logger.trace("start busInitializeLog");
		String version = input.takeString(9);

		logger.info("SCS Gate: Version {}", version);
		state = state.next();
	}

	public void sendRow(UByte[] sendRow) {
		List<UByte> list = Arrays.asList(sendRow);
		String values = ArrayUtils.bytesToHex(list);
		logger.debug("SendRow {}", values);

		try {
			for (int i = 0; i < sendRow.length; i++) {
				out.write(sendRow[i].byteValue());
			}

			out.flush();

		} catch (IOException e) {
			logger.error("Error:", e);
		}
	}

	@Override
	public void sendCommand(SCSMsg msg) {
		SCSConverter scsConverter = new SCSConverter();
		UByte[] sendRow = scsConverter.convertFromSCS(msg);
		logger.debug("Send {} -> {}", msg, sendRow);

		sendRow(sendRow);
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
			listener.SCSValueChanged(event);
	}

	/**
	 * Remove from serialEvent for make little method. Search to understand the message
	 * and encode it.
	 */
	private void retrieveMessage() {
		SCSMsg msg = null;
		SCSConverter scsConverter = new SCSConverter();

		do {
			List<UByte> rowMsg = new ArrayList<>();

			// Take first character for understand the message
			UByte b = input.take();

			// I search a normal message
			if (b.intValue() == SCSConverter.MSG_START) {
				rowMsg.add(b);

				// I add all byte until I find the close byte
				do {
					b = input.take();
					rowMsg.add(b);
				} while (b.intValue() != SCSConverter.MSG_END);

			} else if (b.intValue() == 0x01) {
				// ACK Message
				rowMsg.add(b);
				rowMsg.add(input.take());

			} else {
				if (b.intValue() == SCSConverter.MSG_SEPARATOR) {
					// No Need this char
					logger.debug("Discard Byte 0x07 -> Separator");

				} else {
					// Discard unknown byte
					String res = ArrayUtils.byteToHex(b);
					logger.debug("Discard byte {}", res);
				}
			}

			if (!rowMsg.isEmpty()) {
				UByte[] list = new UByte[rowMsg.size()];

				try {
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

	public static void main(String[] args) throws IOException {
		SCSGate engine = new SCSGate();
		engine.start();

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

//		UByte[] sendRow = ArrayUtils.stringToArray("07:a8:24:ca:12:01:fd:a3");
		UByte[] sendRow = ArrayUtils.stringToArray("07:a8:24:20:12:00:16:a3:01:a5"); //ON
		//07:a8:24:20:12:01:17:a3 OFF
		engine.sendRow(sendRow);

		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
		}

//		sendRow = ArrayUtils.stringToArray("07:a8:24:ca:15:00:fb:a3");
		sendRow = ArrayUtils.stringToArray("07:a8:b8:24:12:00:8e:a3");
		//07:a8:b8:24:12:01:8f:a3
		engine.sendRow(sendRow);

		logger.debug("Bytes wait to write: {}", engine.getSerial().bytesAwaitingWrite());

//		//row.send("@q");
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		engine.close();
	}
}
