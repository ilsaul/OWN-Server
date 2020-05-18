/*
 * Copyright (C) 2010-2020 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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
package org.programmatori.domotica.own.server;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.programmatori.domotica.own.server.engine.EngineManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class wait for client that use tcp/ip protocol for connect to SCS Bus.
 * For any client connected the server create a thread  {@link ClientConnection}
 * for manage it.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 16/10/2010
 */
public class TcpIpServer implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(TcpIpServer.class);

	private ServerSocket serverSocket;
	private final EngineManager engine;
	private final int maxConnections;

	/**
	 * Default Constructor.
	 */
	public TcpIpServer(String configFile) {
		Thread.currentThread().setName("TCP/IP Server");
		Config.getInstance().addThread(Thread.currentThread());

		if (configFile != null) {
			Config.getInstance().setConfig(configFile);
		}

		String line1 = Config.SERVER_NAME + " is Copyright (C) 2010-2020 Moreno Cattaneo";
		String line2 = "This program comes with ABSOLUTELY NO WARRANTY.";
		String line3 = "This is free software, and you are welcome to redistribute it";
		String line4 = "under certain conditions.";
		String line5 = "----";

		logger.info(line1);
		logger.info(line2);
		logger.info(line3);
		logger.info(line4);
		logger.info(line5);
		logger.info("{} v.{} Start", Config.SERVER_NAME, Config.SERVER_VERSION);

		// start the bus manager
		engine = new EngineManagerImpl();
		engine.start();

		final int port = Config.getInstance().getServerPort();
		try {
			logger.info("Opening port {} for receive connections", port);
			this.serverSocket = new ServerSocket(port);

		} catch (IOException e) {
			logger.error("Could not open port {}", port);
			System.exit(-1);
		}

		maxConnections = Config.getInstance().getMaxConnections();
		logger.debug("Max connections: {}", maxConnections);
	}

	/**
	 * Main Loop of the server.
	 * This loop accept connection from the client.
	 */
	@Override
	public void run() {
		ExecutorService pool = Executors.newFixedThreadPool(maxConnections);

		while (!Config.getInstance().isExit()) {
			try {
				// Connection respond
				pool.submit(new ClientConnection(this.serverSocket.accept(), this.engine));

			} catch (IOException e) {
				logger.error("Connection not accepted", e);
			}
		}

		pool.shutdownNow();
		logger.info("Server End");
	}

	/**
	 * For start the Server
	 */
	public static void main(String[] args) {
		// Load config
		String configFile = null;
		if (args.length > 0) {
			configFile = args[0];
		}

		TcpIpServer server = new TcpIpServer(configFile);
		server.run();
	}
}
