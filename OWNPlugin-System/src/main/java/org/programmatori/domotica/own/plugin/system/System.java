/*
 * Copyright (C) 2010-2016 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.Value;
import org.programmatori.domotica.own.sdk.msg.Who;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.programmatori.domotica.own.sdk.server.engine.PlugIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * System manage the base command of the GateWay
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.2 10/08/2016
 */
public class System extends Thread implements PlugIn {
	/** log for the class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(System.class);

	/** 13 = Gateway. */
	public static final int MUST_WHO = 13;

	/** Manage the time of the Gateway. */
	private static final int PM_TIME = 0;
	/** Get the Date of the Gateway. */
	private static final int PM_DATE = 1;
	/** Get the IP of the Gateway. */
	private static final int PM_IP = 10;
	/** Get the NetMask of the Gateway. */
	private static final int PM_NETMASK = 11;
	/** Get the Mac Address of the Gateway. */
	private static final int PM_MAC_ADDRESS = 12;
	/** Get the Model of the Gateway. */
	private static final int PM_SERVER_MODEL = 15;
	/** Get the Firmware version of the Gateway. */
	private static final int PM_FIRMWARE_VERSION = 16;
	/** Get the Starting time of the Gateway. */
	private static final int PM_STARTUP_TIME = 19;
	/** Get the current Time and Date of the Gateway. */
	private static final int PM_TIME_DATE = 22;
	/** Get the Kernel version of the Gateway. */
	private static final int PM_KERNEL_VERSION = 23;
	/** Get the Distributin version of the Gateway. */
	private static final int PM_DISTRIBUTION_VERSION = 24;

	private EngineManager engine;

	public System(EngineManager engine) {
		setName("System");
		this.engine = engine;
	}

	@Override
	public void reciveMsg(SCSMsg msg) {
		LOGGER.debug("System recived msg: {}", msg);
		Value value = null;
		SCSMsg msgResonse = null;

		if (msg.getWho().getMain() == MUST_WHO && msg.isStatus()) {

			// Chose the Command for the output
			switch (msg.getProperty().getMain()) {
				case PM_TIME:
					if (msg.isStatusProperty()) {
						msgResonse = this.setTime(msg);
					} else {
						value = this.getTime();
					}
					break;

				case PM_DATE:
					value = this.getDate();
					break;

				case PM_IP:
					value = this.getIP();
					break;

				case PM_NETMASK:
					value = this.getNetMask();
					break;

				case PM_MAC_ADDRESS:
					value = this.getMac();
					break;

				case PM_SERVER_MODEL:
					value = this.getModel();
					break;

				case PM_FIRMWARE_VERSION:
					value = this.getFirmware();
					break;

				case PM_STARTUP_TIME:
					value = this.getStartUpTime();
					break;

				case PM_TIME_DATE:
					value = this.getTimeDate();
					break;

				case PM_KERNEL_VERSION:
					value = this.getKernel();
					break;

				case PM_DISTRIBUTION_VERSION:
					value = this.getVersion();
					break;

				default:
					LOGGER.warn("Function not implemented: {}", msg.getProperty().getMain());
			}

			if (value != null) {
				final Who who = new Who(Integer.toString(MUST_WHO));
				msgResonse = new SCSMsg(who, true, msg.getWhere(), null, msg.getProperty(), value);
			}

			if (msgResonse != null) {
				// Test purpose
				if (this.engine == null) {
					LOGGER.debug("msg: {}", msgResonse);
				} else {
					this.engine.sendCommand(msgResonse, this);
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
			LOGGER.error("Error in getVersion", e);
		}
		if (firmware == null) {
			firmware = Config.SERVER_VERSION;
		}

		return devideString(firmware, '.');
	}

	private Value getKernel() {
		String kernel = null;
		try {
			kernel = Config.getInstance().getNode("system.kernel");
		} catch (Exception e) {
			// Stub !!!
		}
		if (kernel == null) {
			kernel = "0.0.0";
		}

		return devideString(kernel, '.');
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

		return devideString(firmware, '.');
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
			LOGGER.error("Error in getModel", e);
		}
		if (model == null) {
			model = "99";
		}

		return new Value(model);
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
			LOGGER.error("Error in getMac", e);
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

			if (v != null)
				v.addValue(ip); // IP End Part
		} catch(Exception e) {
			LOGGER.error("Error in getNetMask", e);
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

			if (v != null)
				v.addValue(ip); // IP End Part
		} catch(Exception e) {
			LOGGER.error("Error in getIP", e);
		}

		return v;
	}

	/**
	 * Date //*#13**1*DW*D*M*Y##.
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
	 * Time //*#13**0*H*M*S*TZ##.
	 */
	private Value getTime() {
		Calendar cal = Config.getInstance().getCurentTime();

		Value v = new Value(String.format("%tH", cal.getTimeInMillis())); // Hour
		v.addValue(String.format("%tM", cal.getTimeInMillis())); // Minutes
		v.addValue(String.format("%tS", cal.getTimeInMillis())); // Seconds

		String tz = String.format("%tz", cal.getTimeInMillis());
		String sign;
		if (tz.startsWith("-")) {
			sign = "1";
		} else {
			sign = "0";
		}
		if (tz.startsWith("-") || tz.startsWith("+"))
			tz = tz.substring(1);
		tz = tz.substring(0, 2);

		v.addValue(sign + tz); // Time Zone SNN (S can be 0 = Positive, 1=Negative) (NN it mean NN hour)

		return v;
	}

	@Override
	public void run() {
		// Stub !!
	}
}

