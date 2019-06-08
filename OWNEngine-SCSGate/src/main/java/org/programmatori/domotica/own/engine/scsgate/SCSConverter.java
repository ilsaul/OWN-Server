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

	public SCSMsg convertToSCS(UByte[] values) {
		logger.trace("start conversion to SCS");

		SCSMsg msg = null;

		if (values[0].intValue() == 0x01) {
			// Particular values

			if (values[1].intValue() == 0xA5) {
				return SCSMsg.MSG_ACK;

			} else {
				List<UByte> list = ArrayUtils.asList(values);
				String sList = ArrayUtils.bytesToHex(list);
				logger.warn("Unknown Value: {}", sList);
			}

		} else if (values[0].shortValue() == MSG_START && values[values.length-1].intValue() == MSG_END) {
			// Normal Message
			Who who = null;
			Where where = null;
			What what = null;

			int bus = 0;
			int wherePosition = 2;
			int statusPosition = 4;

			// check Hash
			if (!check(ArrayUtils.subArray(values, 1, values.length-2), values[values.length-2])) {
				logger.warn("Hash from bus:{} calc:{}", values[values.length-2], calcHash(ArrayUtils.subArray(values, 1, values.length-2)));
			}

			if (values[1].intValue() == 0xe4) {
				// Command for SCS Bus connected trow device 422
				//es.: e4:01:00:00:24:ca
				bus = values[2].intValue();

				wherePosition += 3;
				statusPosition += 4;
			}

			// Request Status Message
			if (values[2].intValue() == 0xCA) {
				where = new Where(ArrayUtils.byteToHex(values[1])); // Destinazione
				who = new Who("1"); // TODO: How to know if is a Who=1 or Who=2
				msg = new SCSMsg(who, true, where, null, null, null);

			} else if (values[1] == Unsigned.ubyte(WHERE_APL) || bus > 0) {
				// The BTicino Configurator is in Hex Value
				where = new Where(ArrayUtils.byteToHex(values[wherePosition])); // Destination
				if (bus > 0) {
					where.addParam("4");
					where.addParam(Integer.toString(bus));
				}

				// Value
				StatusValue status = StatusValue.findByByte(values[statusPosition]);
				if (status == null) {
					logger.warn("Unknown value for message: {}", values[statusPosition]);
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

			} else if (values[1] == Unsigned.ubyte(0xB1)) {
				// I know only one *#1*0##
				who = new Who("1");
				where = new Where("0");
				msg = new SCSMsg(who, true, where, null, null, null);

			} else if (values[1] == Unsigned.ubyte(0x24)) {
				// Status
				logger.warn("Not Implemented 0x24");
				// a8:24:20:12:00:16:a3
				// a8:24:ca:12:01:fd:a3

			} else {
				List<UByte> list = ArrayUtils.asList(values);
				String sList = ArrayUtils.bytesToHex(list);
				logger.warn("Unknown Value: {}", sList);
			}
		} else {
			List<UByte> list = ArrayUtils.asList(values);
			String sList = ArrayUtils.bytesToHex(list);
			logger.warn("Unknown Value: {}", sList);
		}

		return msg;
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

		} else if (where.countParams() == 2 && where.getParams(0).equals("4")) {
			// Other Bus
			msg.add(UByte.valueOf(OTHER_BUS_START));
			msg.add(UByte.valueOf(where.getParams(1))); // Bus Number
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
}
