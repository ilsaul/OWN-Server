package org.programmatori.domotica.own.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.Permission;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.server.Controller;
import org.programmatori.domotica.own.server.TcpIpServer;

public class SCSServerTest {
	private TcpIpServer server;

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

	@Before
	public void setUp() throws Exception {
		System.setSecurityManager(new NoExitSecurityManager());
	}

	@After
	public void tearDown() throws Exception {
		System.setSecurityManager(null); // or save and restore original
	}

	@Ignore("disable becase need remake")
	@Test
	public void testStartUp() {
		startUpServer();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			fail("Non deve essere interrotto");
		}

		assertEquals(true, server != null);
		Config.getInstance().setExit(true);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			fail("Non deve essere interrotto");
		}
	}

	@Ignore("disable becase need remake")
	@Test
	public void testClientTimeOut() {
		startUpServer();

		SCSClient client = new SCSClient("127.0.0.1", 20000);
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
			fail("Non deve essere interrotto");
		}
	}

	@Ignore("disable becase need remake")
	@Test
	public void testClientSendCommand() {
		clientSend("*1*1*11##");
	}

	@Ignore("disable becase need remake")
	@Test
	public void testClientSendStatusOne() {
		clientSend("*#1*11##");
	}

	private void startUpServer() {
		try {
			Controller contr = new Controller(Config.DEFAULT_CONFIG_PATH + "/configTest.xml");
			//Controller contr = new Controller();
			contr.start();
		} catch (ExitException e) {
			fail("Exit status :" + e.status);
		}
	}

	public void clientSend(String command) {
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

}
