package org.programmatori.domotica.own.plugin.system;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.Value;

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
	public void testReceiveMsg() {
		TestEngine engine = new TestEngine();
		System sys = new System(engine);

		try {
			SCSMsg msg = new SCSMsg("*#13**0##"); // *#13**0*O*M*S*F##
			Calendar now = Config.getInstance().getCurentTime();
			sys.receiveMsg(msg);
			String time = getTime(now);
			assertEquals("wrong Time",new SCSMsg("*#13**0*" + time + "##"), engine.msgs.poll());

			msg = new SCSMsg("*#13**1##"); //*#13**1*DW*D*M*Y##
			//Calendar now = Config.getInstance().getCurentTime();
			sys.receiveMsg(msg);
			String date = getDate(now);
			assertEquals("wrong Date",new SCSMsg("*#13**1*" + date + "##"), engine.msgs.poll());


			//TODO: Creare tutti i test per le funzioni
//			msg = new SCSMsg("*#13**10##"); //*#13**10*IP1*IP2*IP3*IP4##
//			sys.receiveMsg(msg);
//			msg = new SCSMsg("*#13**11##"); //*#13**10*MASK1*MASK2*MASK3*MASK4##
//			sys.receiveMsg(msg);
//			msg = new SCSMsg("*#13**12##"); //*#13**10*MAC1*MAC2*MAC3*MAC4##
//			sys.receiveMsg(msg);
//			msg = new SCSMsg("*#13**15##"); //*#13**10*Version##
//			sys.receiveMsg(msg);
//			msg = new SCSMsg("*#13**16##"); //*#13**10*Firmware##
//			sys.receiveMsg(msg);
//			msg = new SCSMsg("*#13**19##"); //*#13**10*H*M*S*TZ*D*M*Y##
//			sys.receiveMsg(msg);
//			msg = new SCSMsg("*#13**22##"); //*#13**10*H*M*S*TZ*D*M*Y##
//			sys.receiveMsg(msg);
//			msg = new SCSMsg("*#13**23##"); //*#13**10*Kernel##
//			sys.receiveMsg(msg);
//			msg = new SCSMsg("*#13**24##"); //*#13**10*Version##
//			sys.receiveMsg(msg);
//			msg = new SCSMsg("*#13**24##"); //*#13**10*Version##
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

}
