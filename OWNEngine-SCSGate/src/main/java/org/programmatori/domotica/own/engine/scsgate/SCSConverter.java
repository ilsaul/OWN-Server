package org.programmatori.domotica.own.engine.scsgate;

import org.joou.UByte;
import org.joou.Unsigned;
import org.programmatori.domotica.own.engine.util.ArrayUtils;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.What;
import org.programmatori.domotica.own.sdk.msg.Where;
import org.programmatori.domotica.own.sdk.msg.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

public class SCSConverter {
	private static final Logger logger = LoggerFactory.getLogger(SCSConverter.class);

	static final int MSG_SEPARATOR = 0x07;
	static final int MSG_START = 0xA8;
	static final int MSG_END = 0xA3;

	private static final int OTHER_BUS_START = 0xE4;
	private static final int OTHER_BUS_END = 0xCA;

	private static final int WHERE_GR = 0xB3;
	private static final int WHERE_GEN = 0xB5;
	private static final int WHERE_APL = 0xB8;

	public SCSMsg convertToSCS(UByte[] values) {
		logger.trace("start conversion to SCS");

		SCSMsg msg = null;

		// Particular values
		if (values[0].intValue() == 0x01 && values[1].intValue() == 0xA5) {
			return SCSMsg.MSG_ACK;
		}

		// Normal Message
		// 0xA8 iniziatore messaggio
		// 0xA3 terminatore di messaggio
		if (values[0].shortValue() == MSG_START && values[values.length-1].intValue() == MSG_END) {
			Who who = null;
			Where where = null;
			What what = null;

			// Controllo il check
			if (!check(ArrayUtils.subArray(values, 1, values.length-2), values[values.length-2])) {
				logger.warn("Check Hash Orig:{} Calc:{}", values[values.length-2], calcHash(ArrayUtils.subArray(values, 1, values.length-2)));
			}

			int bus = 0;
			int wherePosition = 2;
			int statusPositon = 4;

			if (values[1].intValue() == 0xe4) {
				// Command for SCS Bus connected trow XXXXX
				//es.: e4:01:00:00:24:ca
				bus = values[2].intValue();

				wherePosition += 3;
				statusPositon += 4;
			}


			// Request Status Message
			if (values[2].intValue() == 0xCA) {
				where = new Where(ArrayUtils.byteToHex(values[1])); // Destinazione
				who = new Who("1"); // TODO: Come faccio a capire che tipo è?

				msg = new SCSMsg(who, true, where, null, null, null);
			}

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

			if (values[1] == Unsigned.ubyte(WHERE_APL) || bus > 0) {
				// The BTicino Configurator is Hex Value
				where = new Where(ArrayUtils.byteToHex(values[wherePosition])); // Destinazione
				if (bus > 0) {
					where.addParam("4");
					where.addParam(Integer.toString(bus));
				}

				// Value
				StatusValue status = StatusValue.findByByte(values[statusPositon]);
				if (status == null) {
					logger.warn("Unknown value for message: {}", values[statusPositon]);
				} else {
					who = new Who(status.getWhoString());
					what = new What(status.getWhatString());
				}

				msg = new SCSMsg(who, where, what);

			} else if (values[1] == Unsigned.ubyte(WHERE_GR)) {
				// The BTicino Configurator is Hex Value
				where = new Where(ArrayUtils.byteToHex(values[2])); // Destination

				if (values[3].byteValue() != 0x12) {
					logger.error("In byte 3 I received a unknown value '{}' I wait '0x12'", ArrayUtils.byteToHex(values[3]));
				}

				// Value
				StatusValue status = StatusValue.findByByte(values[4]);
				who = new Who(status.getWhoString());
				what = new What(status.getWhatString());

				msg = new SCSMsg(who, where, what);

			} else if (values[1] == Unsigned.ubyte(WHERE_GEN)) {
				//a8:b5:09:12:0a:a4:a3 -> *2*0*##
				// GENerale alle tapparelle Down

				// The BTicino Configurator is Hex Value
				if (values[2].byteValue() == 9) {
					where = new Where("");
				} else {
					logger.error("In byte 2 I received a unknown value '{}' I wait '9'", ArrayUtils.byteToHex(values[2]));
				}

				if (values[3].byteValue() != 0x12) {
					logger.error("In byte 3 I received a unknown value '{}' I wait '0x12'", ArrayUtils.byteToHex(values[3]));
				}

				// Value
				StatusValue status = StatusValue.findByByte(values[4]);
				if (status == null) {
					logger.warn("Unknown value for message: {}", values[4]);
				} else {
					who = new Who(status.getWhoString());
					what = new What(status.getWhatString());
				}

				msg = new SCSMsg(who, where, what);


			} else if (values[1] == Unsigned.ubyte(0x24)) {
				// Status
				logger.warn("Not Implemented 0x24");


			} else {
				logger.warn("Not Implemented");
				// a8:24:20:12:00:16:a3
				// a8:24:ca:12:01:fd:a3
				// Comando
				//who = null;
				//where = new Where(ArrayUtils.byteToHex(values[2])); // Destinazione
				//what = new What(String.valueOf(values[4].byteValue())); // Comando
				//if (values[2] == Unsigned.ubyte(0x30)) {
				//	who = new Who("1"); // Mittente
				//}

				//msg = new SCSMsg(who, where, what);
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
		return check == calcHash(values);
	}

	private UByte calcHash(UByte[] values) {
		UByte ret = values[0];

		for (int i = 1; i < values.length; i++) {
			ret = ArrayUtils.logicalXOR(ret, values[i]);
		}

		return ret;
	}

	public UByte[] convertFromSCS(SCSMsg scsMsg) {
		Deque<UByte> msg = new ArrayDeque<>();

		Where where = scsMsg.getWhere();
		UByte destination = null;
		if (where.getMain() > -1) {
			String sWhere = where.getSMain(); // Area + PL
			destination = ArrayUtils.hexToByte(sWhere);
		}

		if (destination == null) {
			// GENeral Command
			msg.add(UByte.valueOf(WHERE_GEN));
			msg.add(UByte.valueOf(9)); // Fix Value 9

		} else if (where.getPL() == 0) { // TODO: scsMsg.isAreaMsg() -> isGroupMsg()
			// GRoup Command
			msg.add(UByte.valueOf(WHERE_GR));
			msg.add(UByte.valueOf(7)); // Fix Value 7

		} else if (where.countParams() == 2 && where.getParams(0).equals("4")) {
			// Other Bus
			msg.add(UByte.valueOf(OTHER_BUS_START));
			msg.add(UByte.valueOf(where.getParams(1))); // Bus Number
			msg.add(UByte.valueOf(0)); // Fix Value 0
			msg.add(UByte.valueOf(0)); // Fix Value 0
			msg.add(destination);
			msg.add(UByte.valueOf(OTHER_BUS_END));

		} else if (scsMsg.isStatus()) {
			msg.add(destination);
			msg.add(UByte.valueOf(0xCA));

		} else {
			// Current Bus
			msg.add(UByte.valueOf(WHERE_APL));
			msg.add(destination);
		}

		if (scsMsg.isStatus()) {
			msg.add(UByte.valueOf(0x15)); // Fix Value
			msg.add(UByte.valueOf(0)); // Fix Value

		} else {
			msg.add(UByte.valueOf(0x12)); // Fix Value

			// What
			Who who = scsMsg.getWho();
			What what = scsMsg.getWhat();
			StatusValue status = StatusValue.findBySCS(who.getMain(), what.getMain());
			msg.add(status.getByteValue());
		}

		UByte[] ret = new UByte[msg.size()];
		msg.add(calcHash(msg.toArray(ret)));

		msg.add(UByte.valueOf(MSG_END));
		msg.push(UByte.valueOf(MSG_START));

		ret = new UByte[msg.size()];

		return msg.toArray(ret);
	}
}
