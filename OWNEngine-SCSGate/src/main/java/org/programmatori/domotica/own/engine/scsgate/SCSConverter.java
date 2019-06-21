package org.programmatori.domotica.own.engine.scsgate;

import org.joou.UByte;
import org.programmatori.domotica.own.engine.util.ArrayUtils;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.Property;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.Value;
import org.programmatori.domotica.own.sdk.msg.What;
import org.programmatori.domotica.own.sdk.msg.Where;
import org.programmatori.domotica.own.sdk.msg.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

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

	private static final int WHERE_OTHER_BUS_START = 0xE4;

	private static final int REQUEST_COMMNAD = 0x12;
	private static final int REQUEST_STATUS = 0x15;

	public SCSMsg convertToSCS(UByte[] values) {
		logger.trace("start conversion to SCS");

		Who who = null;
		Where where = null; // The BTicino Configurator is in Hex Value
		What what = null;
		boolean statusWho = false;
		Property property = null;
		Value value = null;

		if (values[0].intValue() == 0x01) {
			// Particular message

			if (values[1].intValue() == 0xA5) { // *#*1##
				where = new Where("1");
				who = new Who(true, "");

			} else if (values[1].intValue() == 0xE9) {
				logUnknown(values, 1); //TODO: What is?

			} else {
				logUnknown(values, 1);
			}

		} else if (values[0].shortValue() == MSG_START && values[values.length - 1].intValue() == MSG_END) {
			// Normal message
			//who = new Who("1"); // Arbitrary value

			int bus = 0;
			int wherePosition = 2;
			int request = 3;
			int statusPosition = 4;

			// check Hash
			if (!check(ArrayUtils.subArray(values, 1, values.length - 2), values[values.length - 2])) {
				logger.warn("Hash from bus:{} calc:{}", values[values.length - 2], calcHash(ArrayUtils.subArray(values, 1, values.length - 2)));
			}

			if (values[1].intValue() == WHERE_OTHER_BUS_START) { //es.: e4:01:00:00:24:ca
				// Command for SCS Bus connected trow device 422
				bus = values[2].intValue();

				request += 4;
				wherePosition += 3;
				statusPosition += 4;

				where = new Where(ArrayUtils.byteToHex(values[wherePosition]));
				if (bus > 0) {
					where.addParam("4");
					where.addParam(Integer.toString(bus));
				}

			} else if (values[1].intValue() == 0xB1) {
				// General Request Status
				//where = new Where(ArrayUtils.byteToHex(values[wherePosition]));

			} else if (values[1].intValue() == WHERE_GR) {
				// Group
				where = new Where(ArrayUtils.byteToHex(values[wherePosition]));

			} else if (values[1].intValue() == WHERE_GEN) { // Ex.:
				// General Request Command

				// Source?
				//if (values[2].intValue() != 0) logUnknown(values, 2);
				//where = new Where(values[wherePosition].toString());

			} else if (values[1].intValue() == WHERE_APL) {
				// Direct Point
				where = new Where(ArrayUtils.byteToHex(values[wherePosition]));
				if (bus > 0) {
					where.addParam("4");
					where.addParam(Integer.toString(bus));
				}

			} else {
				wherePosition = 1;
				where = new Where(ArrayUtils.byteToHex(values[wherePosition]));

				if (0xCA == values[2].intValue()) {
					logger.debug("Send by L4686SDK");
				} else {
					if (where.getArea() * 10 != Integer.parseInt(ArrayUtils.byteToHex(values[2]))) {
						logUnknown(values, 2);
						return null;
					}
				}
			}

			if (values[request].intValue() == REQUEST_COMMNAD) {
				// Stub
			} else if (values[request].intValue() == REQUEST_STATUS) {
				statusWho = true;
			} else {
				logUnknown(values, request);
			}

			StatusValue status = StatusValue.findByByte(values[statusPosition]);
			what = new What(status.getWhatString());
			who = new Who(statusWho, String.valueOf(status.getWho()));

			if (statusWho) {
				// If is a status what must be 0
				if (values[statusPosition].intValue() == 0) {
					what = new What("0");
				}
			}
		}

		SCSMsg msg = null;
		try {
			msg = new SCSMsg(who, where, what, property, value);
		} catch (MessageFormatException e) {

			logger.error("Conversion to SCS Error {}", bytesToString(values), e);
		}

		return msg;
	}

	private String bytesToString(UByte[] msg) {
		List<UByte> list = ArrayUtils.asList(msg);

		return ArrayUtils.bytesToHex(list);
	}

	private void logUnknown(UByte[] msg, int index) {
		String sList = bytesToString(msg);
		String sValue = ArrayUtils.byteToHex(msg[index]);

		logger.warn("Unknown value {} in position {} of message {}", sValue, index, sList);
	}

	public UByte[] convertFromSCS(SCSMsg scsMsg) {
		logger.trace("start conversion from SCS");

		Deque<UByte> msg = new ArrayDeque<>();

		Where where = scsMsg.getWhere();
		UByte destination = null;
		if (where.getMain() > -1) {
			String sWhere = where.getSMain(); // Area + PL

			if (sWhere.length() == 1) {
				sWhere = "0" + sWhere;
			}

			destination = ArrayUtils.hexToByte(sWhere);
		}

		if (destination == null) {
			// GENeral Command
			msg.add(UByte.valueOf(WHERE_GEN));
			msg.add(UByte.valueOf(9)); // Fix Value 9

		} else if (where.getPL() == 0 && where.getArea() > 0) { // TODO: scsMsg.isAreaMsg() -> isGroupMsg()
			// GRoup Command
			msg.add(UByte.valueOf(WHERE_GR));
			msg.add(UByte.valueOf(7)); // Fix Value 7

		} else if (where.countParams() == 2 && where.getLevel() == 4) {
			// Other Bus
			msg.add(UByte.valueOf(OTHER_BUS_START));
			msg.add(UByte.valueOf(where.getAddress())); // Bus Number
			msg.add(UByte.valueOf(0)); // Fix Value 0
			msg.add(UByte.valueOf(0)); // Fix Value 0
			msg.add(destination);
			msg.add(UByte.valueOf(OTHER_BUS_END));

		} else if (scsMsg.isStatus()) {
			if (destination.intValue() == 0) {
				msg.add(UByte.valueOf(0xB1));
				msg.add(destination); // I'm not sure is destination or only a zero value
			} else {
				msg.add(destination);
				msg.add(UByte.valueOf(0xCA));
			}

		} else {
			// Current Bus
			//msg.add(UByte.valueOf(WHERE_APL));
			msg.add(destination);
			msg.add(UByte.valueOf(0xCA)); // Mittente
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

	private boolean check(UByte[] values, UByte check) {
		return check == calcHash(values);
	}

	public static UByte calcHash(UByte[] values) {
		UByte ret = values[0];

		for (int i = 1; i < values.length; i++) {
			ret = ArrayUtils.logicalXOR(ret, values[i]);
		}

		return ret;
	}
}
