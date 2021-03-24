package org.programmatori.domotica.own.test;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.iot.own.server.OWNServer;
import org.programmatori.iot.own.server.network.TcpIpServer;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.fail;

public class ServerLauncher extends Thread {
	private OWNServer server;

	@Override
	public synchronized void start() {
		super.start();

		try {
			URL resource = getClass().getClassLoader().getResource("config-Test.xml");
			server = new OWNServer(new File(resource.toURI()));

		} catch (SCSServerTest.ExitException | URISyntaxException e) {
			fail("Exit status :" + e.toString());
		}
	}

	@Override
	public void run() {
		super.run();
		server.run();
	}
}
