package org.programmatori.domotica.own.engine.scsgate;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
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
import java.util.List;
import java.util.TooManyListenersException;

/**
 * This Engine use a SGSGate make from Guido Pic for connect OWN Server with the real SCS Bus. I don't sell any hardware
 * for use with this software because I made for my own use.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.1.0, 05/06/2018
 */
public class SCSGate extends Serial implements Engine, SerialPortDataListener {
	private static final Logger logger = LoggerFactory.getLogger(SCSGate.class);

	private static final String WELCOME = "Started!\r\n";

	private SCSGateState state;
	private OutputStream out;
	private List<SCSListener> listListener;

	private InputManager input = new InputManager();

	public void SCSGate() {
		findSerial(Config.getInstance().getNode("scsgate"));
		// I leave default setting

		state = SCSGateState.STATE_INITIAL;
	}

	@Override
	public boolean isReady() {
		return isConnected();
	}

	@Override
	protected boolean connect() throws IOException {
		if (super.connect()) {
			getSerial().addDataListener(this);
			out = getSerial().getOutputStream();

			return busInitialize();
		}

		return false;
	}

	private boolean busInitialize() {
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

		return state == SCSGateState.STATE_SCS_GATE_READY;
	}

	private void busInitializeInitial(String errorMsg) {
		StringBuilder value = new StringBuilder();

		while (!WELCOME.equals(value.toString())) {
			value.append(input.takeChar());

			if (!WELCOME.startsWith(value.toString())) {
				while (!WELCOME.startsWith(value.toString()) && value.length() > 0) {
					value.delete(1,1); //TODO: Test if work
				}
			}
		}

		if (WELCOME.equals(value.toString())) {
			state = state.next(); //RowEngineState.STATE_ARDUINO_READY;ø
			logger.info(state.getDescription());
		} else {
			logger.error(errorMsg, state.next().getDescription());
		}
	}

	private void busInitializeReady(String errorMsg, String setMsg) {
		char ch = input.takeChar();

		if (ch == 'k') {
			state = state.next();
			logger.info(setMsg, state.getDescription());
		} else {
			logger.error(errorMsg, state.next().getDescription());
		}
	}

	private void busInitializeSpeed(String errorMsg, String setMsg) {
		char ch = input.takeChar();

		if (ch == 'k') {
			state = state.next();
			logger.info(setMsg, state.getDescription());
		} else {
			logger.error(errorMsg, state.next().getDescription());
		}
	}

	private void busInitializeVolt(String errorMsg, String setMsg) {
		char ch = input.takeChar();

		if (ch == 'k') {
			state = state.next();
			logger.info(setMsg, state.getDescription());
		} else {
			logger.error(errorMsg, state.next().getDescription());
		}
	}

	private void busInitializeAscii(String errorMsg, String setMsg) {
		char ch = input.takeChar();

		if (ch == 'k') {
			state = state.next(); // I must change always state
			logger.info(setMsg, state.getDescription());
		} else {
			logger.error(errorMsg, state.next().getDescription());
		}
	}

	private void busInitializeLog() {
		String version = input.takeString(9);

		logger.info("SCS Gate: Version {}", version);
		state = state.next();
	}

	@Override
	public void sendCommand(SCSMsg msg) {
		SCSConverter scsConverter = new SCSConverter();
		UByte[] sendRow = scsConverter.convertFromSCS(msg);
		logger.debug("Send {} -> {}", msg, sendRow);

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
				input.add(b);
			}

			// Search message
			retrieveMessage();


		} else if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN) {
			logger.warn("Event Data Written");
		}
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
				msg = scsConverter.convertToSCS(rowMsg.toArray(list));

				// Log
				String row = ArrayUtils.bytesToHex(rowMsg);
				logger.debug("Row: {} -> SCS: {}", row, msg);

				if (msg != null) Config.getInstance().getMessageLog().log(msg.toString());

				SCSEvent event = new SCSEvent(this, msg);
				notifyListeners(event);
			}

		} while (input.count() != 0);
	}
}
