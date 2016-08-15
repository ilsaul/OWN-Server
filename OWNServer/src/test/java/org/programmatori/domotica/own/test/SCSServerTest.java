/*
 * OWN Server is
 * Copyright (C) 2010-2012 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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

import java.security.Permission;
import java.util.Calendar;

import junit.framework.*;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.server.Controller;
import org.programmatori.domotica.own.server.TcpIpServer;

public class SCSServerTest extends TestCase {
	TcpIpServer server;

	protected static class ExitException extends SecurityException {
		private static final long serialVersionUID = -2845481264737315099L;
		public final int status;

		public ExitException(int status) {
			super("There is no escape!");
			this.status = status;
		}
	}

	private static class NoExitSecurityManager extends SecurityManager {
		@Override
		public void checkPermission(Permission perm) {
			// allow anything.
		}

		@Override
		public void checkPermission(Permission perm, Object context) {
			// allow anything.
		}

		@Override
		public void checkExit(int status) {
			super.checkExit(status);
			throw new ExitException(status);
		}
	}

	public SCSServerTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setSecurityManager(new NoExitSecurityManager());
	}

	@Override
	protected void tearDown() throws Exception {
		System.setSecurityManager(null); // or save and restore original
		super.tearDown();
	}

	private void startUpServer() {
		try {
			Controller contr = new Controller();
			//server = new TcpIpServer();
			//Thread t = new Thread(server);
			//t.start();
			contr.start();
		} catch (ExitException e) {
			fail("Exit status :" + e.status);
			// Maybe wrong configuration Engine
		}
	}

	public void testStartUp() {
		startUpServer();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// stub!!
		}

		assertEquals(true, server != null);
		Config.getInstance().setExit(true);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void testClientTimeOut() {
		startUpServer();

		SCSClient client = new SCSClient("127.0.0.1", 20000);
		//client.start();
		client.connect(null);
		long startTime = Calendar.getInstance().getTimeInMillis();
		long endTime = 0;
		try {
			client.checkTimeOut();
		} catch (Exception e) {
			endTime = Calendar.getInstance().getTimeInMillis();
		}

		long time = endTime - startTime;
		long defaultTime = Config.getInstance().getWelcomeTimeout();
		long delta = 1000;
		assertTrue("The Timeout Welcome is not correct: " + time,(defaultTime - delta < time) && (defaultTime + delta > time));

		//fail("Not yet implemented");
		Config.getInstance().setExit(true);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void testClientSendCommand() {
		testClientSend("*1*1*11##");
	}

	public void testClientSendStatusOne() {
		testClientSend("*#1*11##");
	}

	public void testClientSend(String command) {
		startUpServer();

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		SCSClient client = new SCSClient("127.0.0.1", 20000);
		//client.start();

		client.connect(SCSClient.MODE_COMMAND);
		String receive = client.send(command);
		assertEquals(SCSMsg.MSG_ACK.toString(), receive);

		//fail("Not yet implemented");
		Config.getInstance().setExit(true);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new SCSServerTest("testStartUp"));
		suite.addTest(new SCSServerTest("testClientTimeOut"));
		suite.addTest(new SCSServerTest("testClientSendCommand"));
		suite.addTest(new SCSServerTest("testClientSendStatusOne"));
		return suite;
	}

}
