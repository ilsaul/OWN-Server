package org.programmatori.domotica.own.plugin.system;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.programmatori.domotica.own.sdk.component.Who;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

import static org.junit.Assert.*;

public class SystemTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSystem() {
		System sys = new System(null);

		assertNotNull("System must be create", sys);
	}

	@Test
	public void testTime() {

		try {
			String initMsg = SCSMsg.MSG_STARTER + "#" + Who.GATEWAY.getValue() + "**" + GatewayFunctions.TIME.getFunctionId();

			Calendar now = Config.getInstance().getCurentTime();
			SCSMsg msg = genericTest(initMsg + SCSMsg.MSG_ENDED); // *#13**0*O*M*S*F##
			String time = getTime(now);
			assertEquals("wrong Time", new SCSMsg(initMsg + SCSMsg.MSG_SEPARATOR + time + SCSMsg.MSG_ENDED), msg);

		} catch (Exception e) {
			fail("It cant shut exception");
		}
	}

	@Test
	public void testDate() {

		try {
			String initMsg = SCSMsg.MSG_STARTER + "#" + Who.GATEWAY.getValue() + "**" + GatewayFunctions.DATE.getFunctionId();

			Calendar now = Config.getInstance().getCurentTime();
			SCSMsg msg = genericTest(initMsg + SCSMsg.MSG_ENDED); // *#13**1*DW*D*M*Y##
			String date = getDate(now);
			assertEquals("wrong Date", new SCSMsg(initMsg + SCSMsg.MSG_SEPARATOR + date + SCSMsg.MSG_ENDED), msg);

		} catch (Exception e) {
			fail("It cant shut exception");
		}
	}

	@Test
	public void testIpV4() {

		try {
			String initMsg = SCSMsg.MSG_STARTER + "#" + Who.GATEWAY.getValue() + "**" + GatewayFunctions.IP.getFunctionId();

			String ip = getAddress().getHostAddress();
			ip = ip.replaceAll("\\.", "*");

			SCSMsg msg = genericTest(initMsg + SCSMsg.MSG_ENDED); // *#13**10*IP1*IP2*IP3*IP4##
			assertEquals("wrong IP", new SCSMsg(initMsg + SCSMsg.MSG_SEPARATOR + ip + SCSMsg.MSG_ENDED), msg);

		} catch (Exception e) {
			fail("It cant shut exception");
		}
	}

	@Test
	public void testNetMask() {

		try {
			String initMsg = SCSMsg.MSG_STARTER + "#" + Who.GATEWAY.getValue() + "**" + GatewayFunctions.NETMASK.getFunctionId();

			String netmask = "";
			InetAddress adr = getAddress();
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(adr);
			List<InterfaceAddress> interfaces = networkInterface.getInterfaceAddresses();

			short mask = 0;
			for (InterfaceAddress ia: interfaces) {
				if (ia.getAddress().equals(adr)) {
					mask = ia.getNetworkPrefixLength();
					break;
				}
			}

			switch (mask) {
				// IPv4
				case 8:
					netmask = "255*0*0*0";
					break;
				case 16:
					netmask = "255*255*0*0";
					break;

				case 24:
					netmask = "255*255*255*0";
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
					fail("Netmask unknown " + mask);
					break;
			}

			SCSMsg msg = genericTest(initMsg + SCSMsg.MSG_ENDED); // *#13**11*MASK1*MASK2*MASK3*MASK4##
			assertEquals("wrong NetMask", new SCSMsg(initMsg + SCSMsg.MSG_SEPARATOR + netmask + SCSMsg.MSG_ENDED), msg);

		} catch (Exception e) {
			fail("It cant shut exception");
		}
	}

	@Test
	public void testModel() {

		try {
			String initMsg = SCSMsg.MSG_STARTER + "#" + Who.GATEWAY.getValue() + "**" + GatewayFunctions.SERVER_MODEL.getFunctionId();

			String model = Integer.toString(GatewayModel.OWN_SERVER.getModelId());

			SCSMsg msg = genericTest(initMsg + SCSMsg.MSG_ENDED); // *#13**15*Model##
			assertEquals("wrong Model", new SCSMsg(initMsg + SCSMsg.MSG_SEPARATOR + model + SCSMsg.MSG_ENDED), msg);

		} catch (Exception e) {
			fail("It cant shut exception");
		}
	}

	@Test
	public void testMac() {

		try {
			String initMsg = SCSMsg.MSG_STARTER + "#" + Who.GATEWAY.getValue() + "**" + GatewayFunctions.MAC_ADDRESS.getFunctionId();

			String mac = "";
			InetAddress adr = getAddress();
			NetworkInterface networkInterface = NetworkInterface.getByInetAddress(adr);
			byte[] idr = networkInterface.getHardwareAddress();

			for (byte b : idr) {
				mac += "*" + (b & 0xFF);
			}
			mac = mac.substring(1);

			SCSMsg msg = genericTest(initMsg + SCSMsg.MSG_ENDED); // *#13**12*IP1*IP2*IP3*IP4##
			assertEquals("wrong Mac", new SCSMsg(initMsg + SCSMsg.MSG_SEPARATOR + mac + SCSMsg.MSG_ENDED), msg);

		} catch (Exception e) {
			fail("It cant shut exception");
		}
	}

	@Test
	public void testFirmware() {

		try {
			String initMsg = SCSMsg.MSG_STARTER + "#" + Who.GATEWAY.getValue() + "**" + GatewayFunctions.FIRMWARE_VERSION.getFunctionId();

			String firmware = "0*0*0";

			SCSMsg msg = genericTest(initMsg + SCSMsg.MSG_ENDED); //*#13**16*Firmware##
			assertEquals("wrong Firmware", new SCSMsg(initMsg + SCSMsg.MSG_SEPARATOR + firmware + SCSMsg.MSG_ENDED), msg);

		} catch (Exception e) {
			fail("It cant shut exception");
		}
	}

	@Test
	public void testKernel() {

		try {
			String initMsg = SCSMsg.MSG_STARTER + "#" + Who.GATEWAY.getValue() + "**" + GatewayFunctions.KERNEL_VERSION.getFunctionId();

			String kernel = "0*0*0";

			SCSMsg msg = genericTest(initMsg + SCSMsg.MSG_ENDED); //*#13**23*Kernel##
			assertEquals("wrong Kernel", new SCSMsg(initMsg + SCSMsg.MSG_SEPARATOR + kernel + SCSMsg.MSG_ENDED), msg);

		} catch (Exception e) {
			fail("It cant shut exception");
		}
	}

	@Test
	public void testVersion() {

		try {
			String initMsg = SCSMsg.MSG_STARTER + "#" + Who.GATEWAY.getValue() + "**" + GatewayFunctions.DISTRIBUTION_VERSION.getFunctionId();

			String version = "0*0*0";

			SCSMsg msg = genericTest(initMsg + SCSMsg.MSG_ENDED); //*#13**24*Version##
			assertEquals("wrong Version", new SCSMsg(initMsg + SCSMsg.MSG_SEPARATOR + version + SCSMsg.MSG_ENDED), msg);

		} catch (Exception e) {
			fail("It cant shut exception");
		}
	}

	@Test
	public void testStartUpTime() {

		try {
			String initMsg = SCSMsg.MSG_STARTER + "#" + Who.GATEWAY.getValue() + "**" + GatewayFunctions.STARTUP_TIME.getFunctionId();
			
			Calendar now = Config.getInstance().getCurentTime();
			SCSMsg msg = genericTest(initMsg + SCSMsg.MSG_ENDED); // *#13**19*H*M*S*TZ*D*M*Y##
			String date = getDate(now);
			assertEquals("wrong Date", new SCSMsg(initMsg + SCSMsg.MSG_SEPARATOR + date + SCSMsg.MSG_ENDED), msg);

		} catch (Exception e) {
			fail("It cant shut exception");
		}
	}

	public void testReceiveMsg() {
		TestEngine engine = new TestEngine();
		System sys = new System(engine);

		try {

			//TODO: Creare tutti i test per le funzioni
//			msg = new SCSMsg("*#13**19##"); //*#13**19*H*M*S*TZ*D*M*Y##
//			sys.receiveMsg(msg);
//			msg = new SCSMsg("*#13**22##"); //*#13**22*H*M*S*TZ*D*M*Y##
//			sys.receiveMsg(msg);
//
//			msg = new SCSMsg("*#13**#0*12*11*01*001##"); //*#13**#0*H*M*S*F## Write Time
//			sys.receiveMsg(msg);
//			msg = new SCSMsg("*#13**#1*12*11*01*001##"); //*#13**#1*DW*D*M*Y## Write Date
//			sys.receiveMsg(msg);
//			msg = new SCSMsg("*#13**#22*12*11*01*001##"); //*#13**#22*H*m*S*F*DW*M*Y## Write Time and Date
//			sys.receiveMsg(msg);



		} catch (MessageFormatException e) {
			e.printStackTrace();
		}


		//fail("Not yet implemented"); // TODO
	}

	private SCSMsg genericTest(String sMsg) {
		TestEngine engine = new TestEngine();
		System sys = new System(engine);
		Config.getInstance().setConfig(ClassLoader.getSystemResourceAsStream("Config.xml"));

		try {
			SCSMsg msg = new SCSMsg(sMsg);
			sys.receiveMsg(msg);

			return engine.msgs.poll();

		} catch (MessageFormatException e) {
			fail("It cant shut exception");
			return null;
		}
	}

	private String getTime(Calendar now) {
		String s = String.format("%tH*", now.getTimeInMillis()); // Hour
		s += String.format("%tM*", now.getTimeInMillis()); // Minutes
		s += String.format("%tS*", now.getTimeInMillis()); // Seconds

		String tz = String.format("%tz", now.getTimeInMillis());
		String sign;
		if (tz.startsWith("-")) {
			sign = "1";
		} else {
			sign = "0";
		}
		if (tz.startsWith("-") || tz.startsWith("+"))
			tz = tz.substring(1);
		tz = tz.substring(0, 2);

		s += sign + tz; // Time Zone SNN (S can be 0 = Positive, 1=Negative) (NN it mean NN hour)

		return s;
	}

	private String getDate(Calendar now) {
		String dw = "0" + (now.get(Calendar.DAY_OF_WEEK)-1);
		if (dw.length() > 2) dw.substring(1);

		String s = dw; // Day in week
		s += String.format("*%td", now.getTimeInMillis()); // Day
		s += String.format("*%tm", now.getTimeInMillis()); // Month
		s += String.format("*%tY", now.getTimeInMillis()); // Year

		return s;
	}

	private InetAddress getAddress() throws SocketException {
		InetAddress thisIp = null;

		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface inter = interfaces.nextElement();

			if (!inter.isLoopback()
				&& !inter.isPointToPoint() // && !inter.getName().startsWith("utun") // AirDrop
				&& inter.isUp()
				&& !inter.isVirtual()
				&& !inter.getName().startsWith("awdl") // Apple Wireless Direct link
				&& !inter.getName().startsWith("llw")) { // ???

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

}
