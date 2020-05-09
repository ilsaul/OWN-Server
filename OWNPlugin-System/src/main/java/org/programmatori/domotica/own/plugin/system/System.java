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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import org.jetbrains.annotations.NotNull;
import org.programmatori.domotica.own.sdk.component.Who;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.ServerMsg;
import org.programmatori.domotica.own.sdk.msg.Value;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.programmatori.domotica.own.sdk.server.engine.PlugIn;
import org.programmatori.domotica.own.sdk.utils.TimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * System manage the base command of the GateWay
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.2 16/08/2016
 */
public class System extends Thread implements PlugIn {
	private static final Logger logger = LoggerFactory.getLogger(System.class);

	private final EngineManager engine;

	public System(EngineManager engine) {
		setName("System");
		this.engine = engine;
	}

	@Override
	public void receiveMsg(SCSMsg msg) {
		logger.debug("System received msg: {}", msg);
		Value value = null;
		SCSMsg msgResponse = null;

		if (msg.getWho().getMain() == Who.GATEWAY.getValue() && msg.isStatus()) {

			GatewayFunctions function = GatewayFunctions.createFromId(msg.getProperty().getMain());
			// Chose the Command for the output
			switch (function) {
				case TIME:
					if (msg.isStatusProperty()) {
						msgResponse = this.setTime(msg);
					} else {
						value = this.getTime();
					}
					break;

				case DATE:
					value = this.getDate();
					break;

				case IP:
					value = this.getIP();
					break;

				case NETMASK:
					value = this.getNetMask();
					break;

				case MAC_ADDRESS:
					value = this.getMac();
					break;

				case SERVER_MODEL:
					value = this.getModel();
					break;

				case FIRMWARE_VERSION:
					value = this.getFirmware();
					break;

				case STARTUP_TIME:
					value = this.getStartUpTime();
					break;

				case TIME_DATE:
					value = this.getTimeDate();
					break;

				case KERNEL_VERSION:
					value = this.getKernel();
					break;

				case DISTRIBUTION_VERSION:
					value = this.getVersion();
					break;

				default:
					logger.warn("Function not implemented: {}", msg.getProperty().getMain());
			}

			if (value != null) {
				final org.programmatori.domotica.own.sdk.msg.Who who = new org.programmatori.domotica.own.sdk.msg.Who(true, Integer.toString(Who.GATEWAY.getValue()));
				try {
					msgResponse = new SCSMsg(who, msg.getWhere(), null, msg.getProperty(), value);
				} catch (MessageFormatException e) {
					logger.error("Create Res to SCS Error", e);
				}
			}

			if (msgResponse != null) {
				// Test purpose
				if (this.engine == null) {
					// Debug purpose
					logger.debug("msg: {}", msgResponse);
				} else {
					this.engine.sendCommand(msgResponse, this);
				}
			}
		}
	}

	private SCSMsg setTime(SCSMsg msg) {
		Calendar newTime = GregorianCalendar.getInstance();

		newTime.set(Calendar.HOUR_OF_DAY, msg.getProperty().getMain());
		newTime.set(Calendar.MINUTE, Integer.parseInt(msg.getProperty().getParam(0)));
		newTime.set(Calendar.SECOND, Integer.parseInt(msg.getProperty().getParam(1)));
		newTime.set(Calendar.ZONE_OFFSET, Integer.parseInt(msg.getProperty().getParam(2)));

		Config.getInstance().setUserTime(newTime);

		return ServerMsg.MSG_ACK.getMsg();
	}

	private Value getVersion() {
		String firmware = null;
		try {
			firmware = Config.getInstance().getNode("system.version");
		} catch (Exception e) {
			logger.error("Error in getVersion", e);
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
			logger.warn("Configuration system.kernel read error", e);
		}
		if (kernel == null) {
			kernel = "0.0.0";
		}

		return devideString(kernel, '.');
	}

	private Value getTimeDate() {
		Value v = getTime();
		Value tmp = getDate();

		v.addValue(tmp.getMainAsString());
		for (int i = 0; i < tmp.countParams(); i++) {
			v.addValue(tmp.getSingleValue(i));
		}

		return v;
	}

	private Value getStartUpTime() {
		/* starting date */
		Calendar start = GregorianCalendar.getInstance();
		try {
			start = Config.getInstance().getStartUpTime();
		} catch (Exception e) {
			logger.warn("Error in reading start up time", e);
		}

	    /* now date */
	    Calendar now = Calendar.getInstance();

	    // Get msec from each, and subtract.
		long diff = now.getTimeInMillis() - start.getTimeInMillis();
	    long g = diff / TimeUtility.millisFromDays(1);

	    diff -= TimeUtility.millisFromDays((int)g);
	    int h = (int) diff / TimeUtility.millisFromHours(1);

	    diff -= TimeUtility.millisFromHours(h);
	    int m = (int) diff / TimeUtility.millisFromMinutes(1);
	    
	    diff -= TimeUtility.millisFromMinutes(m);
	    int s = (int) diff / TimeUtility.millisFromSeconds(1);

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
			logger.warn("Configuration system.firmware read error", e);
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
		GatewayModel model = null;
		try {
			model = GatewayModel.createById(Integer.valueOf(Config.getInstance().getNode("system.model")));
		} catch (Exception e) {
			logger.error("Error in getModel", e);
		}
		if (model == null) {
			model = GatewayModel.OWN_SERVER;
		}

		return new Value(Integer.toString(model.getModelId()));
	}

	private Value getMac() {
		Value v = null;

		try {
			InetAddress thisIp = getAddress();

			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(thisIp);
			byte[] idr = networkInterface.getHardwareAddress();

			if (idr != null && idr.length > 0) {
				String val;

				for (byte b : idr) {
					if (b < 0) {
						val = Integer.toString(b & 0xFF);
					} else {
						val = Integer.toString(b);
					}

					if (v == null) {
						v = new Value(val);
					} else {
						v.addValue(val);
					}
				}
			}
		} catch(Exception e) {
			logger.error("Error in getMac", e);
		}

		return v;
	}

	private Value getNetMask() {
		Value v = null;

		try {
			InetAddress thisIp = getAddress();
			String ip = "";

			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(thisIp);
			List<InterfaceAddress> interfaces = networkInterface.getInterfaceAddresses();

			short mask = 0;
			for (InterfaceAddress ia: interfaces) {
				if (ia.getAddress().equals(thisIp)) {
					mask = ia.getNetworkPrefixLength();
					break;
				}
			}

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

			v = createValue(ip);
		} catch(Exception e) {
			logger.error("Error in getNetMask", e);
		}

		return v;
	}

	private Value getIP() {
		Value v = null;

		try {
			InetAddress thisIp = getAddress();
			String ip = thisIp.getHostAddress();

			v = createValue(ip);
		} catch(Exception e) {
			logger.error("Error in getIP", e);
		}

		return v;
	}

	private InetAddress getAddress() throws SocketException {
		InetAddress thisIp = null;

		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface inter = interfaces.nextElement();
			EncapsulationNetworkInterface inte = new EncapsulationNetworkInterface(inter);

			if (!inter.isLoopback()
				&& !inter.isPointToPoint()
				&& inter.isUp()
				&& !inter.isVirtual()
				&& !inter.getName().startsWith("awdl") // Apple Wireless Direct link
				&& !inter.getName().startsWith("llw")) { // ???
				logger.debug("Interface: {}", inte);

				Enumeration<InetAddress> address = inter.getInetAddresses();
				Inet6Address ipv6 = null;
				while (address.hasMoreElements()) {
					InetAddress tempIp = address.nextElement();

					if (tempIp instanceof Inet6Address) {
						ipv6 = (Inet6Address) tempIp;
					} else if (tempIp instanceof Inet4Address) {
						thisIp = tempIp;
					}
				}

				// If not exist ipv4 i use ipv6
				if (thisIp == null) {
					thisIp = ipv6;
				}

			}
		}

		return thisIp;
	}

	@NotNull
	private Value createValue(String ip) {
		Value v = null;

		String[] ipPart = ip.split("\\.");

		// IPv4 have 3 part divide by point
		for (int i = 0; i < ipPart.length; i++) {
			if (i == 0) {
				v = new Value(ipPart[i]); // IP Part
			} else {
				v.addValue(ipPart[i]); // IP Part
			}
		}

		return v;
	}

	/**
	 * Date //*#13**1*DW*D*M*Y##.
	 */
	private Value getDate() {
		Calendar cal = Config.getInstance().getCurentTime();

		String dw = "0" + (cal.get(Calendar.DAY_OF_WEEK)-1);
		if (dw.length() > 2) dw = dw.substring(1);

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
