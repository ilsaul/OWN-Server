/*
 * OWN Server is
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
package org.programmatori.domotica.own.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.What;
import org.programmatori.domotica.own.sdk.msg.Where;
import org.programmatori.domotica.own.sdk.msg.Who;

public class SCSMsgTest {

	public void testGeneric(String msg) {
		SCSMsg scsMsg = null;
		try {
			scsMsg = new SCSMsg(msg);

			assertEquals("Wrong return mag", msg, scsMsg.toString());
		} catch (MessageFormatException e) {
			fail("message " + msg + "not accepted");
		}
	}

	@Test
	public void testAckMsg() {
		testGeneric("*#*1##");
	}

	@Test
	public void testNormalMsg() {
		testGeneric("*1*0*11##");
	}

	@Test
	public void testNormalWithParamMsg() {
		testGeneric("*2*1*41#4#2##");
	}

	@Test
	public void testRequestStatusMsg() {
		testGeneric("*1*11##");
	}

	@Test
	public void testRequestStatusWithParamMsg() {
		testGeneric("*#1*41#4#2##");
	}

	@Test
	public void testRequestValueMsg() {
		testGeneric("*#4*1*0##");
	}

	@Test
	public void testRequestValueTimeMsg() {
		testGeneric("*#13**1##");
	}

	@Test
	public void testSetValueMsg() {
		testGeneric("*#4*1*14*0250##");
	}

	@Test
	public void testSetValueWithParamMsg() {
		testGeneric("*#13**#0*21*10*00*01##");
	}

	@Test
	public void testOtherMsg() {
		testGeneric("*#4*#0*#0*0250*1##");
	}

	@Test
	public void testDouble() {
		testGeneric("*#*1##*1*0*71##");
	}

	@Test
	public void testConnection() {
		testGeneric("*99*0##");
	}

	@Test
	public void testPowereReturn() {
		testGeneric("*#3*10*0*1*2*3*4##");
	}

	@Test
	public void testArea() {
		String msg = "*2*1*41#4#2##"; // <-- Manage sub bus (41 = A=4 PL=1) (4 = local bus, 3=master bus) (I1I2 = 2)

		SCSMsg scsMsg = null;
		try {
			scsMsg = new SCSMsg(msg);

			assertEquals("Wrong Area", 4, scsMsg.getWhere().getArea());
		} catch (MessageFormatException e) {
			fail("message " + msg + "not accepted");
		}
	}

//	public void testLightPoint() {
//		String msg = "*2*1*41#4#2##";
//
//		SCSMsg scsMsg = null;
//		try {
//			scsMsg = new SCSMsg(msg);
//
//			assertEquals("Wrong Light Point", 1, scsMsg.getWhere().getPL());
//		} catch (MessageFormatException e) {
//			fail("message " + msg + "not accepted");
//		}
//	}

	@Test
	public void testLevel() {
		String msg = "*2*1*41#4#2##";

		SCSMsg scsMsg = null;
		try {
			scsMsg = new SCSMsg(msg);

			assertEquals("Wrong Level", 4, scsMsg.getWhere().getLevel());
		} catch (MessageFormatException e) {
			fail("message " + msg + "not accepted");
		}
	}

	@Test
	public void testAddress() {
		String msg = "*2*1*41#4#2##";

		SCSMsg scsMsg = null;
		try {
			scsMsg = new SCSMsg(msg);

			assertEquals("Wrong Address", 2, scsMsg.getWhere().getAddress());
		} catch (MessageFormatException e) {
			fail("message " + msg + "not accepted");
		}
	}

	@Test
	public void testLevelNull() {
		String msg = "*2*1*41##";

		SCSMsg scsMsg = null;
		try {
			scsMsg = new SCSMsg(msg);

			assertEquals("Wrong Level", -1, scsMsg.getWhere().getLevel());
		} catch (MessageFormatException e) {
			fail("message " + msg + "not accepted");
		}
	}

	@Test
	public void testAddressNull() {
		String msg = "*2*1*41##";

		SCSMsg scsMsg = null;
		try {
			scsMsg = new SCSMsg(msg);

			assertEquals("Wrong Address", -1, scsMsg.getWhere().getAddress());
		} catch (MessageFormatException e) {
			fail("message " + msg + "not accepted");
		}
	}

	@Test
	public void testGroup() {
		String msg = "*2*1*7##";

		SCSMsg scsMsg = null;
		try {
			scsMsg = new SCSMsg(msg);

			assertEquals("Wrong Group", -1, scsMsg.getWhere().getAddress());
		} catch (MessageFormatException e) {
			fail("message " + msg + "not accepted");
		}
	}

	@Test
	public void testGENReturn() {
		testGeneric("*2*0*##");
	}



	@Test
	public void testCreateGEN() {
		SCSMsg msg = new SCSMsg(new Who("2"), new Where(""), new What("0"));

		assertEquals("Wrong Create GEN", "*2*0*##", msg.toString());
	}
}
