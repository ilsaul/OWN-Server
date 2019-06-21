/*
 * OWN Server is
 * Copyright (C) 2010-2019 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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
package org.programmatori.domotica.own.test;

import org.junit.Assert;
import org.junit.Test;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.Property;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.Value;
import org.programmatori.domotica.own.sdk.msg.What;
import org.programmatori.domotica.own.sdk.msg.Where;
import org.programmatori.domotica.own.sdk.msg.Who;

public class SCSMsgTest {

	public void generateFormString(String msg, String sAssert) {
		SCSMsg scsMsg = null;
		try {
			scsMsg = new SCSMsg(msg);

			Assert.assertEquals(sAssert, msg, scsMsg.toString());
		} catch (MessageFormatException e) {
			Assert.fail("Message " + msg + " not correct");
		}
	}

	private void generateFormObjects(String SCSMsg, Who who, Where where, What what, Property property, Value value, String assertMsg) {
		try {
			SCSMsg msg = new SCSMsg(who, where, what, property, value);
			Assert.assertEquals(assertMsg, SCSMsg, msg.toString());

		} catch (MessageFormatException e) {
			Assert.fail(assertMsg + " can't throw an exception");
		}
	}

	@Test
	public void testCreateMsgFromString() {

		// Request Commands
		generateFormString("*1*0*24##", "Light - Turn OFF");
		generateFormString("*1*1*24##", "Light - Turn ON");
		generateFormString("*1*11*24##", "Light - Timed 1min Turn ON");
		generateFormString("*1*29*24##", "Light - Timed 2min Turn ON");
		generateFormString("*1*1000#0*24##", "Light - Blink 5sec");
		generateFormString("*1*0#5*24##", "Dimmer - Turn OFF at 5 speed");
		generateFormString("*1*1#5*24##", "Dimmer - Turn ON at 5 speed");
		generateFormString("*1*2*24##", "Dimmer - Turn 20%");
		generateFormString("*1*10*24##", "Dimmer - Turn 100%");
		generateFormString("*14*0*24##", "Disable - Light 24");
		generateFormString("*14*1*24##", "Enable - Light 24");

		generateFormString("*2*0*26##", "Blind - Stop");
		generateFormString("*2*1*26##", "Blind - Up");
		generateFormString("*2*2*26##", "Blind - Down");
		generateFormString("*2*11#1#1*26##", "Blind - UpAdvanced 1step 1priority");

		generateFormString("*2*1*41#4#2##", "testNormalWithParamMsg (Other Bus)");
		//generateFormString("*1*11##", "testRequestStatusMsg"); //???

		generateFormString("*1*0*##", "General OFF");

		//TODO: General Command
		//TODO: Area Command

		// Request Status
		generateFormString("*#1*24##", "Light - RequestStatus");
		//generateFormString("*#1*24#1*995##", "set up the level at X speed - Dimension 1");
		generateFormString("*#2*26##", "Blind - RequestStatus");
		generateFormString("*#2*26*10##", "Blind - RequestStatus - Dimension 10");


		generateFormString("*#1*1*0##", "testRequestValueMsg");
		generateFormString("*#1*41#4#2##", "testRequestStatusWithParamMsg");
		generateFormString("*#13**1##","testRequestValueTimeMsg");
		generateFormString("*#4*1*14*0250##", "testSetValueMsg");
		generateFormString("*#13**#0*21*10*00*01##", "testSetValueMsg");
		//generateFormString("*#13**#0*21*10*00*01##", "testSetValueWithParamMsg");
		generateFormString("*#4*#0*#0*0250*1##", "testOtherMsg");
		generateFormString("*#*1##*1*0*71##", "testDouble");

		generateFormString("*#3*10*0*1*2*3*4##", "testPowereReturn");

		// Other Message
		generateFormString("*#*1##", "testAckMsg");
		generateFormString("*#*0##", "testNackMsg");

		generateFormString("*99*0##", "testConnection");

		generateFormString("*#1*24*1*100*5##", "Dimmer replay for *1*0#5*24##");
		generateFormString("*#1*10*24##", "Dimmer replay status");
	}

	@Test
	public void testCreateMessageFromObjects() {
		generateFormObjects("*#*1##", new Who(true, ""), new Where("1"), null,null,null,"Light - request command");

		generateFormObjects("*1*0*24##", new Who("1"), new Where("24"), new What("0"),null,null,"Light - request command");
		generateFormObjects("*#1*24##", new Who(true,"1"), new Where("24"), null,null,null,"Light - request status");

		generateFormObjects("*2*0*##", new Who("2"), new Where(""), new What("0"),null,null, "testCreateGEN");
		generateFormObjects("*2*0##", new Who("2"),null, new What("0"),null,null, "testCreateGEN 2");

		generateFormObjects("*#4*#0*#0*0250*1##", new Who(true,"4"),new Where(true,"0"),null, new Property(true, "0"),new Value("0250", "1"), "testOtherMsg 2");
		generateFormObjects("*#13**#0*21*10*00*01##", new Who(true, "13"), new Where(""),null, new Property(true, "0"), new Value("21", "10", "00", "01"), "testSetValueMsg 2");

		generateFormObjects("*#1*41#4#2##", new Who(true, "1"), new Where(false,"41","4", "2"), null, null, null, "Message for other bus");
	}

	@Test
	public void testArea() {
		SCSMsg scsMsg = null;

		String msg = "*2*1*51#4#2##"; // <-- Manage sub bus (41 = A=4 PL=1) (4 = local bus, 3=master bus) (I1I2 = 02)
		try {
			scsMsg = new SCSMsg(msg);

			Assert.assertEquals("Wrong Area", 5, scsMsg.getWhere().getArea());
		} catch (MessageFormatException e) {
			Assert.fail("message " + msg + "not accepted");
		}

		msg = "*2*1*07##";
		try {
			scsMsg = new SCSMsg(msg);

			Assert.assertEquals("Wrong Area", 7, scsMsg.getWhere().getArea());
		} catch (MessageFormatException e) {
			Assert.fail("message " + msg + "not accepted");
		}
	}

	@Test
	public void testLightPoint() {
		SCSMsg scsMsg = null;

		String msg = "*2*1*41#4#2##";
		try {
			scsMsg = new SCSMsg(msg);

			Assert.assertEquals("Wrong Light Point", 1, scsMsg.getWhere().getPL());
		} catch (MessageFormatException e) {
			Assert.fail("message " + msg + "not accepted");
		}

		msg = "*2*1*07##";
		try {
			scsMsg = new SCSMsg(msg);

			Assert.assertEquals("Wrong Area", 0, scsMsg.getWhere().getPL());
		} catch (MessageFormatException e) {
			Assert.fail("message " + msg + "not accepted");
		}
	}

	@Test
	public void testLevel() {
		String msg = "*2*1*41#4#2##";

		SCSMsg scsMsg = null;
		try {
			scsMsg = new SCSMsg(msg);

			Assert.assertEquals("Wrong Level", 4, scsMsg.getWhere().getLevel());
		} catch (MessageFormatException e) {
			Assert.fail("message " + msg + "not accepted");
		}
	}

	@Test
	public void testAddress() {
		String msg = "*2*1*41#4#2##";

		SCSMsg scsMsg = null;
		try {
			scsMsg = new SCSMsg(msg);

			Assert.assertEquals("Wrong Address", 2, scsMsg.getWhere().getAddress());
		} catch (MessageFormatException e) {
			Assert.fail("message " + msg + "not accepted");
		}
	}

	@Test
	public void testLevelNull() {
		String msg = "*2*1*41##";

		SCSMsg scsMsg = null;
		try {
			scsMsg = new SCSMsg(msg);

			Assert.assertEquals("Wrong Level", -1, scsMsg.getWhere().getLevel());
		} catch (MessageFormatException e) {
			Assert.fail("message " + msg + "not accepted");
		}
	}

	@Test
	public void testAddressNull() {
		String msg = "*2*1*41##";

		SCSMsg scsMsg = null;
		try {
			scsMsg = new SCSMsg(msg);

			Assert.assertEquals("Wrong Address", -1, scsMsg.getWhere().getAddress());
		} catch (MessageFormatException e) {
			Assert.fail("message " + msg + "not accepted");
		}
	}

	@Test
	public void testGroup() {
		String msg = "*2*1*07##";

		SCSMsg scsMsg = null;
		try {
			scsMsg = new SCSMsg(msg);

			Assert.assertEquals("Wrong Group", -1, scsMsg.getWhere().getAddress());
		} catch (MessageFormatException e) {
			Assert.fail("message " + msg + "not accepted");
		}
	}
}
