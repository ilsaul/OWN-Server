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
package org.programmatori.domotica.own.plugin.system;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.*;
import org.programmatori.domotica.own.sdk.server.engine.PlugIn;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * System manage the base command of the GateWay
 * @version 0.1 07/01/2011
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 */
public class System extends Thread implements PlugIn {
	private static final Logger logger = LoggerFactory.getLogger(System.class);

	public static final int MUST_WHO = 13; // 13 = Gateway

	private EngineManager engine;

	public System(EngineManager engine) {
		setName("System");
		this.engine = engine;
	}

	@Override
	public void reciveMsg(SCSMsg msg) {
		logger.debug("System recived msg: {}", msg);
		Value value = null;
		SCSMsg msgResonse = null;

		if (msg.getWho().getMain() == MUST_WHO && msg.isStatus()) {

			switch (msg.getProperty().getMain()) {
				case 0: // Time
					if (msg.isStatusProperty()) {
						msgResonse = setTime(msg);

					} else {
						value = getTime();
					}
					break;

				case 1: // Date
					value = getDate();
					break;

				case 10: // IP
					value = getIP();
					break;

				case 11: // NetMask
					value = getNetMask();
					break;

				case 12: // Mac Address
					value = getMac();
					break;

				case 15: // Server Model
					value = getModel();
					break;

				case 16: // Firmware Version
					value = getFirmware();
					break;

				case 19: // Start-up time
					value = getStartUpTime();
					break;

				case 22: // Time & Date
					value = getTimeDate();
					break;

				case 23: // Kernel Version
					value = getKernel();
					break;

				case 24: // Distribution Version
					value = getVersion();
					break;
			}

			if (value != null) {
				Who who = new Who("" + MUST_WHO);
				msgResonse = new SCSMsg(who, true, msg.getWhere(), null, msg.getProperty(), value);
			}

			if (msgResonse != null) {
				// Test purpose
				if (engine == null) {
					java.lang.System.out.println("msg: " + msgResonse);
				} else {
					engine.sendCommand(msgResonse, this);
				}
			}

		} else {
			// ignore other message
		}

	}

	private SCSMsg setTime(SCSMsg msg) {
		Calendar newTime = GregorianCalendar.getInstance();

		newTime.set(Calendar.HOUR_OF_DAY, msg.getProperty().getMain());
		newTime.set(Calendar.MINUTE, Integer.parseInt(msg.getProperty().getParams(0)));
		newTime.set(Calendar.SECOND, Integer.parseInt(msg.getProperty().getParams(1)));
		newTime.set(Calendar.ZONE_OFFSET, Integer.parseInt(msg.getProperty().getParams(2)));

		Config.getInstance().setUserTime(newTime);

		return SCSMsg.MSG_ACK;
	}

	private Value getVersion() {
		String firmware = null;
		try {
			firmware = Config.getInstance().getNode("system.version");
		} catch (Exception e) {

		}
		if (firmware == null) {
			firmware = Config.SERVER_VERSION;
		}

		Value v = devideString(firmware, '.');

		return v;
	}

	private Value getKernel() {
		String kernel = null;
		try {
			kernel = Config.getInstance().getNode("system.kernel");
		} catch (Exception e) {

		}
		if (kernel == null) {
			kernel = "0.0.0";
		}

		Value v = devideString(kernel, '.');

		return v;
	}

	private Value getTimeDate() {
		Value v = getTime();
		Value tmp = getDate();

		v.addValue(tmp.getSMain());
		for (int i = 0; i < tmp.countParams(); i++) {
			v.addValue(tmp.getSingleValue(i));
		}

		return v;
	}

	private Value getStartUpTime() {
		//Calendar now = GregorianCalendar.getInstance();
		Calendar start = GregorianCalendar.getInstance();
		try {
			start = Config.getInstance().getStartUpTime();
		} catch (Exception e) {

		}

		/** The date */
	    Date d1 = start.getTime();

	    /** Today's date */
	    Date today = new Date();

	    // Get msec from each, and subtract.
	    long diff = today.getTime() - d1.getTime();

	    //java.lang.System.out.println("The 21st century (up to " + today + ") is " + (diff / (1000 * 60 * 60 * 24)) + " days old.");

	    long g = diff / (1000 * 60 * 60 * 24);
	    diff -= g * 24 * 60 * 60 * 1000;
	    long h = diff / (1000 * 60 * 60);
	    diff -= h * 60 * 60 * 1000;
	    long m = diff / (1000 * 60);
	    diff -= m * 60 * 1000;
	    long s = diff / 1000;

	    //Value v = null;
		Value v = new Value(String.format("%02d", g)); // Day
		v.addValue(String.format("%02d", h)); // Hour
		v.addValue(String.format("%02d", m)); // Minutes
		v.addValue(String.format("%02d", s)); // Seconds

		return v;
	}

	private Value getFirmware() {
		String firmware = null;
		try {
			firmware = Config.getInstance().getNode("system.firmware");
		} catch (Exception e) {

		}
		if (firmware == null) {
			firmware = "0.0.0";
		}

		Value v = devideString(firmware, '.');

		return v;
	}

	private Value devideString(String str, char devideKey) {
		StringTokenizer st = new StringTokenizer(str, "" + devideKey);

		Value v = null;
		boolean first = true;

		while (st.hasMoreElements()) {
			String val = (String) st.nextElement();

			if (first) {
				v = new Value(val);
				first = false;
			} else {
				v.addValue(val);
			}
		}

		return v;
	}

	/**
	 * The Model know by BTicino is:<br>
	 * 2) MHServer<br>
	 * 4) MH2000<br>
	 * 6) F452<br>
	 * 7) F452V<br>
	 * 11) MHServer2<br>
	 * 13) H4684<br>
	 * <br>
	 * I use 99 for OWNServer<br>
	 */
	private Value getModel() {
		String model = null;
		try {
			model = Config.getInstance().getNode("system.model");
		} catch (Exception e) {

		}
		if (model == null) {
			model = "99";
		}
		Value v = new Value(model);
	return v;
	}

	private Value getMac() {
		Value v = null;

		try {
			InetAddress thisIp = InetAddress.getLocalHost();
			boolean first = true;

			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(thisIp);
			byte[] idr = networkInterface.getHardwareAddress();

			if (idr != null && idr.length > 0) {
				String val = "0";
				for(int z=0; z<idr.length; z++) {
					if (idr[z] < 0) {
						val = Integer.toString(256 + idr[z]);
					} else {
						val = Integer.toString(idr[z]);
					}

					if (first) {
						v = new Value(val);
						first = false;
					} else {
						v.addValue(val);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		return v;
	}

	private Value getNetMask() {
		Value v = null;

		try {
			InetAddress thisIp = InetAddress.getLocalHost();
			String ip = "";

			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(thisIp);
			short mask = networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength();

			switch (mask) {
			// IPv4
			case 8:
				ip = "255.0.0.0";
				break;
			case 16:
				ip = "255.255.0.0";
				break;

			case 24:
				ip = "255.255.255.0";
				break;

			// IPv6
//			case 128:
//				ip = "::1/128";
//				break;
//
//			case 10:
//				ip = "fe80::203:baff:fe27:1243/10";
//				break;

			default:
				ip = "255.255.255.0";
				break;
			}

			for (int i = 0; i < 3; i++) {
				if (i == 0) {
					v = new Value(ip.substring(0, ip.indexOf('.'))); // IP Part
				} else {
					v.addValue(ip.substring(0, ip.indexOf('.'))); // IP Part
				}
				ip = ip.substring(ip.indexOf('.')+1);
			}
			v.addValue(ip); // IP End Part
		} catch(Exception e) {
			e.printStackTrace();
		}

		return v;
	}

	private Value getIP() {
		Value v = null;

		try {
			InetAddress thisIp = InetAddress.getLocalHost();
			String ip = thisIp.getHostAddress();

			for (int i = 0; i < 3; i++) {
				if (i == 0) {
					v = new Value(ip.substring(0, ip.indexOf('.'))); // IP Part
				} else {
					v.addValue(ip.substring(0, ip.indexOf('.'))); // IP Part
				}
				ip = ip.substring(ip.indexOf('.')+1);
			}
			v.addValue(ip); // IP End Part
		} catch(Exception e) {
			e.printStackTrace();
		}

		return v;
	}

	/**
	 * Date //*#13**1*DW*D*M*Y##
	 */
	private Value getDate() {
		Calendar cal = Config.getInstance().getCurentTime();

		String dw = "0" + (cal.get(Calendar.DAY_OF_WEEK)-1);
		if (dw.length() > 2) dw.substring(1);

		Value v = new Value(dw); // Day in week
		v.addValue(String.format("%td", cal.getTimeInMillis())); // Day
		v.addValue(String.format("%tm", cal.getTimeInMillis())); // Month
		v.addValue(String.format("%tY", cal.getTimeInMillis())); // Year

		return v;
	}

	/**
	 * Time //*#13**0*H*M*S*TZ##
	 */
	private Value getTime() {
		Calendar cal = Config.getInstance().getCurentTime();

		Value v = new Value(String.format("%tH", cal.getTimeInMillis())); // Hour
		v.addValue(String.format("%tM", cal.getTimeInMillis())); // Minutes
		v.addValue(String.format("%tS", cal.getTimeInMillis())); // Seconds

		String tz = String.format("%tz", cal.getTimeInMillis());
		String sign = "";
		if (tz.startsWith("-")) {
			sign = "1";
		} else {
			sign = "0";
		}
		if (tz.startsWith("-") || tz.startsWith("+")) tz = tz.substring(1);
		tz = tz.substring(0, 2);

		v.addValue(sign + tz); // Time Zone SNN (S can be 0 = Positive, 1=Negative) (NN it mean NN hour)

		return v;
	}

	@Override
	public void run() {

	}

	/**
	 * Test
	 */
	public static void main(String[] args) {
		System sys = new System(null);

		try {
			SCSMsg msg = new SCSMsg("*#13**0##"); // *#13**0*O*M*S*F##
			sys.reciveMsg(msg);
			msg = new SCSMsg("*#13**1##"); //*#13**1*DW*D*M*Y##
			sys.reciveMsg(msg);
			msg = new SCSMsg("*#13**10##"); //*#13**10*IP1*IP2*IP3*IP4##
			sys.reciveMsg(msg);
			msg = new SCSMsg("*#13**11##"); //*#13**10*MASK1*MASK2*MASK3*MASK4##
			sys.reciveMsg(msg);
			msg = new SCSMsg("*#13**12##"); //*#13**10*MAC1*MAC2*MAC3*MAC4##
			sys.reciveMsg(msg);
			msg = new SCSMsg("*#13**15##"); //*#13**10*Version##
			sys.reciveMsg(msg);
			msg = new SCSMsg("*#13**16##"); //*#13**10*Firmware##
			sys.reciveMsg(msg);
			msg = new SCSMsg("*#13**19##"); //*#13**10*H*M*S*TZ*D*M*Y##
			sys.reciveMsg(msg);
			msg = new SCSMsg("*#13**22##"); //*#13**10*H*M*S*TZ*D*M*Y##
			sys.reciveMsg(msg);
			msg = new SCSMsg("*#13**23##"); //*#13**10*Kernel##
			sys.reciveMsg(msg);
			msg = new SCSMsg("*#13**24##"); //*#13**10*Version##
			sys.reciveMsg(msg);
			msg = new SCSMsg("*#13**24##"); //*#13**10*Version##
			sys.reciveMsg(msg);

			msg = new SCSMsg("*#13**#0*12*11*01*001##"); //*#13**#0*H*M*S*F## Write Time
			sys.reciveMsg(msg);
			msg = new SCSMsg("*#13**#1*12*11*01*001##"); //*#13**#1*DW*D*M*Y## Write Date
			sys.reciveMsg(msg);
			msg = new SCSMsg("*#13**#22*12*11*01*001##"); //*#13**#22*H*m*S*F*DW*M*Y## Write Time and Date
			sys.reciveMsg(msg);



		} catch (MessageFormatException e) {
			e.printStackTrace();
		}

	}

}

