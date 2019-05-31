package org.programmatori.domotica.own.engine.row;

import com.fazecast.jSerialComm.SerialPort;
import org.joou.UByte;
import org.joou.Unsigned;
import org.programmatori.domotica.own.engine.util.ArrayUtils;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.What;
import org.programmatori.domotica.own.sdk.msg.Where;
import org.programmatori.domotica.own.sdk.msg.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SCSGate {
	private static final Logger logger = LoggerFactory.getLogger(SCSGate.class);

	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 115200;

	private static final String WELCOME = "Started!\r\n";
	private static final String SERIAL = "IOUSBHostDevice"; //"cu.usbmodem1421 - IOUSBHostDevice"

	private static final int MSG_START = 0xA8;
	private static final int MSG_END = 0xA3;
	private static final int WHERE_GR = 0xB3;
	private static final int WHERE_GEN = 0xB5;
	private static final int WHERE_APL = 0xB8;

	private SerialPort currentSerial;
	private OutputStream out;
	private SCSGateState state;
	private InputReceiver receiver = new InputReceiver();

	/**
	 * Find Serial
	 */
	public SCSGate() {
		logger.trace("Trace: ENBLED");
		logger.debug("Debug: ENBLED");

		SerialPort[] ports = SerialPort.getCommPorts();
		state = SCSGateState.STATE_INITIAL;

		currentSerial = null;
		for (int i = 0; i < ports.length; i++) {
			SerialPort serialPort = ports[i];

			logger.debug("Porta: {}->{}", serialPort.getSystemPortName(), serialPort.getDescriptivePortName());

			// I can't find a better system than use a name
			if (serialPort.getDescriptivePortName().equals(SERIAL)) {
				logger.info("Port found {}", serialPort.getSystemPortName());
				currentSerial = serialPort;
				currentSerial.setBaudRate(DATA_RATE);
			}
		}
	}

	public boolean isConnected() {
		return state == SCSGateState.STATE_SCS_GATE_READY;
	}

	/**
	 * Connect to serial
	 */
	public void connect() {
		// SCSGate driver = new SCSGate(currentSerial, this, )
		boolean connected = currentSerial.openPort();
		logger.info("Connection: {}", connected);

		if (connected) {
			currentSerial.addDataListener(receiver);
			out = currentSerial.getOutputStream();
			// try { Thread.sleep(5000); } catch (Exception e) { e.printStackTrace(); }

			busInitialize();
		}
	}

	private void busInitialize() {
		String errorMsg = "Error to {}";
		String setMsg = "Settled {}";

		// Check state
		if (state == SCSGateState.STATE_INITIAL) {
			StringBuilder value = new StringBuilder();
			while (!WELCOME.equals(value.toString())) {
				value.append(receiver.takeChar());
				if (!WELCOME.startsWith(value.toString())) {
					while (!WELCOME.startsWith(value.toString()) && value.length() > 0) {
						value.delete(1,1); //TODO: Test if work
					}
				}
			}

			if (WELCOME.equals(value.toString())) {
				state = state.next(); //RowEngineState.STATE_ARDUINO_READY;
				logger.info(state.getDescription());
			} else {
				logger.error(errorMsg, state.next().getDescription());
			}
		}

		if (state == SCSGateState.STATE_ARDUINO_READY) {
			char ch =  receiver.takeChar();

			if (ch == 'k') {
				state = state.next();
				logger.info(setMsg, state.getDescription());
			} else {
				logger.error(errorMsg, state.next().getDescription());
			}
		}

		if (state == SCSGateState.STATE_SCS_GATE_SET_SLOW_SPEED) {
			char ch = receiver.takeChar();

			if (ch == 'k') {
				state = state.next();
				logger.info(setMsg, state.getDescription());
			} else {
				logger.error(errorMsg, state.next().getDescription());
			}
		}

		if (state == SCSGateState.STATE_SCS_GATE_SET_VOLT) {
			char ch = receiver.takeChar();

			if (ch == 'k') {
				state = state.next();
				logger.info(setMsg, state.getDescription());
			} else {
				logger.error(errorMsg, state.next().getDescription());
			}
		}

		if (state == SCSGateState.STATE_SCS_GATE_SET_ASCII) {
			char ch = (char) receiver.take().byteValue();

			if (ch == 'k') {
				state = state.next(); // I must change always state
				logger.info(setMsg, state.getDescription());
			} else {
				logger.error(errorMsg, state.next().getDescription());
			}
		}

		if (state == SCSGateState.STATE_SCS_GATE_SET_LOG) {
			String version = receiver.takeString(9);

			logger.info("SCS Gate: Version {}", version);
			state = state.next(); //STATE_SCS_GATE_READY;
		}
	}

	public void send(UByte[] msg) throws IOException {
		logger.debug("Send {}", msg);

		for (int i = 0; i < msg.length; i++) {
			out.write(msg[i].byteValue());
		}

		out.flush();
	}

	private SCSMsg convertToSCS(UByte[] values) {
		System.out.println("Test");
		SCSMsg msg = null;

		if (values.length == 1) {
			//TODO: Convertire

			//MSG_NACK  = new SCSMsg("*#*0##");
			if (values[0].intValue() ==  0xA5) {
				return SCSMsg.MSG_ACK;
			}
			//MSG_NOP   = new SCSMsg("*#*2##");
			//MSG_RET   = new SCSMsg("*#*3##");
			//MSG_COLL  = new SCSMsg("*#*4##");
			//MSG_NOBUS = new SCSMsg("*#*5##");
			//MSG_USY   = new SCSMsg("*#*6##");
			//MSG_PROC  = new SCSMsg("*#*7##");


		// 0xA8 iniziatore messaggio
		// 0xA3 terminatore di messaggio
		} else if (values[0].shortValue() == MSG_START && values[values.length-1].intValue() == MSG_END) {
			Who who = null;
			Where where = null;
			What what = null;
			// Controllo il check
			//if (check(ArrayUtils.subArray(values, 1, values.length-2), values[values.length-2])) {

				// Where, What, property. Value, statusWho, statusWhere, statusProperty

				// Aree
				// 1 Bagno di Servizio
				// 2 Bagno Idromassaggio
				// 3 Balconi
				// 4 Disimpegni
				// 5 Cameretta
				// 6 Studio
				// 7 Sala e Cucina
				// 8 Camera Matrimoniale
				// 9 Cabina Armadio

			if (values[1] == Unsigned.ubyte(WHERE_APL)) {
				// The BTicino Configurator is Hex Value
				where = new Where(ArrayUtils.byteToHex(values[2])); // Destinazione

				// Value
				StatusValue status = StatusValue.getStatusByValue(values[4].byteValue());
				switch (status) {
					case OFF: who = new Who(StatusValue.OFF.getWho());
							what = new What(StatusValue.OFF.getWhat()); break;

					case ON: who = new Who(StatusValue.ON.getWho());
							what = new What(StatusValue.ON.getWhat()); break;

					case UP: who = new Who(StatusValue.UP.getWho());
							what = new What(StatusValue.UP.getWhat()); break;

					case DOWN: who = new Who(StatusValue.DOWN.getWho());
							what = new What(StatusValue.DOWN.getWhat()); break;

					case STOP: who = new Who(StatusValue.STOP.getWho());
						what = new What(StatusValue.STOP.getWhat()); break;

					default: logger.warn("Unknown value for message: {}", values[4]);
				}

				msg = new SCSMsg(who, where, what);

			} else if (values[1] == Unsigned.ubyte(WHERE_GR)) {
				// The BTicino Configurator is Hex Value
				where = new Where(ArrayUtils.byteToHex(values[2])); // Destinazione

				if (values[3].byteValue() != 0x12) {
					logger.error("In byte 3 I received a unknown value '{}' I wait '0x12'",  ArrayUtils.byteToHex(values[3]));
				}

				// Value
				StatusValue status = StatusValue.getStatusByValue(values[4].byteValue());
				switch (status) {
					case OFF: who = new Who(StatusValue.OFF.getWho());
						what = new What(StatusValue.OFF.getWhat()); break;

					case ON: who = new Who(StatusValue.ON.getWho());
						what = new What(StatusValue.ON.getWhat()); break;

					case UP: who = new Who(StatusValue.UP.getWho());
						what = new What(StatusValue.UP.getWhat()); break;

					case DOWN: who = new Who(StatusValue.DOWN.getWho());
						what = new What(StatusValue.DOWN.getWhat()); break;

					case STOP: who = new Who(StatusValue.STOP.getWho());
						what = new What(StatusValue.STOP.getWhat()); break;

					default: logger.warn("Unknown value for message: {}", values[4]);
				}

				msg = new SCSMsg(who, where, what);

			} else if (values[1] == Unsigned.ubyte(WHERE_GEN)) {
				//a8:b5:09:12:0a:a4:a3 -> *2*0*##
				// GENerale alle tapparelle Down

				// The BTicino Configurator is Hex Value
				if (values[2].byteValue() == 9) {
					where = new Where("");
				} else {
					logger.error("In byte 2 I received a unknown value '{}' I wait '9'",  ArrayUtils.byteToHex(values[2]));
				}

				if (values[3].byteValue() != 0x12) {
					logger.error("In byte 3 I received a unknown value '{}' I wait '0x12'",  ArrayUtils.byteToHex(values[3]));
				}

				// Value
				StatusValue status = StatusValue.getStatusByValue(values[4].byteValue());
				switch (status) {
					case OFF: who = new Who(StatusValue.OFF.getWho());
						what = new What(StatusValue.OFF.getWhat()); break;

					case ON: who = new Who(StatusValue.ON.getWho());
						what = new What(StatusValue.ON.getWhat()); break;

					case UP: who = new Who(StatusValue.UP.getWho());
						what = new What(StatusValue.UP.getWhat()); break;

					case DOWN: who = new Who(StatusValue.DOWN.getWho());
						what = new What(StatusValue.DOWN.getWhat()); break;

					case STOP: who = new Who(StatusValue.STOP.getWho());
						what = new What(StatusValue.STOP.getWhat()); break;

					default: logger.warn("Unknown value for message: {}", values[4]);
				}

				msg = new SCSMsg(who, where, what);


			} else {
				// a8:24:20:12:00:16:a3
				// Comando
				who = null;
				where = new Where(ArrayUtils.byteToHex(values[2])); // Destinazione
				what = new What(String.valueOf(values[4].byteValue())); // Comando
				if (values[2] == Unsigned.ubyte(0x30)) {
					who = new Who("1"); // Mittente
				}

				msg = new SCSMsg(who, where, what);
			}
		}

		// Balcone (Area 3)
		// SCS[0]: A8 B8 32 12 00 98 A3 | 32 destinatario
		// SCS[1]: A8 32 30 12 01 11 A3 | Stato Accesso

		// Stessa stanza Soggiorno (Area 7)
		// SCS[3]: A8 B8 73 12 01 D8 A3
		// SCS[4]: A8 73 70 12 00 11 A3
		// SCS[3]: A8 B8 72 12 00 D8 A3 Stato Spento
		// SCS[4]: A8 72 70 12 01 11 A3

		// Tapparella Balcone Studio (Area 6)
		// SCS[3]: A8 B8 62 12 09 C1 A3 Giu 09
		// SCS[4]: A8 B8 62 12 0A C2 A3 Su 0A

		// Tutto OFF
		// SCS[2]: A8 B1 00 12 01 A2 A3 Tutti 00
		// SCS[3]: A8 48 40 12 00 1A A3
		// SCS[4]: A5
		// SCS[5]: A8 B8 48 12 00 E2 A3

		// Sconosciuto (Forse Power)
		// A8 B7 01 13 04 A1 A3
		// A8 B7 02 13 04 A2 A3
		// A8 B7 03 13 04 A3 A3
		// A8 B7 04 13 04 A4 A3
		// A8 B7 05 13 04 A5 A3
		// A8 B7 06 13 04 A6 A3
		// A8 B7 07 13 04 A7 A3
		// A8 B7 07 13 04 A7 A3
		// A8 B7 08 13 04 A8 A3

		// Richiesta Status
		//a8:24:ca:15:00:fb:a3 -> *#1*24##
		//a8:48:ca:15:00:97:a3 -> *#1*48##
		//01:a5: -> ACK (*#*1##)
		//07
		//a8:b8:48:12:01:e3:a3 -> Comando e Status
		//a8:00:ca:1c:80:56:a3 -> NACK (*#*0##)
		//a8:05:ca:1c:80:53:a3 -> MSG_NOBUS (*#*5##)


		// 0xA8 iniziatore di comando
		// 0x33 destinazione: codice dispositivo (corrisponde alla targhetta inserita sull'attuatore)
		// 0x00
		// 0x15 Fisso? richiesta di stato
		// 0x00 nuovo valore di stato
		// 0x26 check byte (è sempre il risultato dell'operazione Xor dei 4 bytes precedenti)
		// 0xA3 terminatore di comando

		// Ack
		// SCS[2]: A5

		// Comando
		// SCS[3]: A8 B8 24 12 00 8E A3
		// 0xA8 iniziatore di comando/stato
		// 0xB8 0xB5 0xB3 APL GR GEN
		// 0x33 provenienza - codice dispositivo (corrisponde alla targhetta inserita sull'attuatore)
		// 0x12 Fisso? richiesta di comando
		// 0x00 stato di acceso (0x01: stato di spento)
		// 0x99 check byte (è sempre il risultato dell'operazione Xor dei 4 bytes precedenti)
		// 0xA3 terminatore di comando/stato

		return msg;
	}

	private boolean check(UByte[] values, UByte check) {
		UByte ret = values[0];

		for (int i = 1; i < values.length; i++) {
			ret = ArrayUtils.logicalXOR(ret, values[i]);
		}

		return ret == check;
	}

	private SCSMsg retrieveMessage() {
		SCSMsg msg = null;
		do {
			UByte b = receiver.take();
			List<UByte> rowMsg = new ArrayList<>();

			while (b.intValue() != 0xA8) {
				rowMsg.add(b);
				UByte list[] = new UByte[rowMsg.size()];
				msg = convertToSCS(rowMsg.toArray(list));

				String res = ArrayUtils.byteToHex(b);
				logger.debug("Discard byte {}", res);
				Config.getInstance().getMessageLog().log(res);
				if (msg != null) {
					Config.getInstance().getMessageLog().log(msg, false, null);
				}
				b = receiver.take();
			}

			rowMsg = new ArrayList<>();
			if (b.intValue() == 0xA8) {

				while (b.intValue() != 0xA3) {
					rowMsg.add(b);
					b = receiver.take();
				}
				rowMsg.add(b);

				logger.debug("row msg: {}", ArrayUtils.bytesToHex(rowMsg));
				Config.getInstance().getMessageLog().log(ArrayUtils.bytesToHex(rowMsg));

				UByte list[] = new UByte[rowMsg.size()];
				msg = convertToSCS(rowMsg.toArray(list));
				logger.debug("Msg: {}", msg);
			}

			//TODO: Ciclare per gli altri
		} while (msg != null);

		return msg;
	}

	public void close() {
		currentSerial.removeDataListener();
		boolean connected = !currentSerial.closePort();

		if (!connected) state = SCSGateState.STATE_INITIAL;

		logger.info("Connection close: {}", !connected);
	}

	public static void main(String[] args) throws IOException {
		SCSGate row = new SCSGate();
		row.connect();

		if (row.isConnected()) {
			SCSMsg msg  = row.retrieveMessage();
			System.out.println(msg);
			UByte[] sendRow = {Unsigned.ubyte(0xA8),
					//a8:b8:62:12:08:c0:a3

					Unsigned.ubyte(0xB8),
					Unsigned.ubyte(0x62),
					Unsigned.ubyte(0x12),
					Unsigned.ubyte(0x08),
					Unsigned.ubyte(0xC0),
					Unsigned.ubyte(0xA3)};


			row.send(sendRow);
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//row.send("@q");
			try {
				Thread.sleep(10000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Port not open");
			logger.warn("Port not open");
		}

		row.close();
	}
}
