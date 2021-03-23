package org.programmatori.domotica.own.test;

import com.github.niqdev.openwebnet.OpenWebNet;
import com.github.niqdev.openwebnet.message.Lighting;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.ServerMsg;
import org.programmatori.iot.own.server.OWNServer;
import org.programmatori.iot.own.server.network.TcpIpServer;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Permission;
import java.util.Calendar;

import static org.junit.Assert.*;

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

	@Ignore("disable because need remake")
	@Test
	public void testStartUp() {
		startUpServer();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			fail("It must not be interrupted");
		}

		assertEquals(true, server != null);
		Config.getInstance().setExit(true);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			fail("It must not be interrupted");
		}
	}

	@Ignore("disable because need remake")
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
			fail("It must not be interrupted");
		}
	}

	@Test
	public void testProva() {
		startUpServer();

		OpenWebNet simpleClient = OpenWebNet.newClient(OpenWebNet.defaultGateway("localhost"));
		simpleClient.send(Lighting.requestStatus("21", Lighting.Type.POINT_TO_POINT, ""))
			.map(Lighting.handleStatus(() -> System.out.println("ON"), () -> System.out.println("OFF")))
			.subscribe(System.out::println);
		Assert.assertNotNull("Client not start", simpleClient);
	}

	@Ignore("disable because need remake")
	@Test
	public void testClientSendCommand() {
		clientSend("*1*1*11##");
	}

	@Ignore("disable because need remake")
	@Test
	public void testClientSendStatusOne() {
		clientSend("*#1*11##");
	}

	private void startUpServer() {
		try {
			URL resource = getClass().getClassLoader().getResource("config-Test.xml");
			OWNServer own = new OWNServer(new File(resource.toURI()));
			own.run();
		} catch (ExitException | URISyntaxException e) {
			fail("Exit status :" + e.toString());
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
		assertEquals(ServerMsg.MSG_ACK.toString(), receive);

		//fail("Not yet implemented");
		Config.getInstance().setExit(true);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
