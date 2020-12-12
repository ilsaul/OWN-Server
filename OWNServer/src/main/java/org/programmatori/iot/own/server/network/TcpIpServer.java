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
package org.programmatori.iot.own.server.network;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The TcpIpServer waits for the connects to the OWN Server.
 * For any client connected, TcpIpServer instantiate a {@link ClientConnection}
 * for manage it.
 * This class initialize the bus system through {@link EngineManager} and use it
 * to let the clients talk with bus.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 16/10/2010
 */
public class TcpIpServer extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(TcpIpServer.class);

	private ServerSocket serverSocket;
	private final EngineManager engineManager;
	private final int maxConnections;

	/**
	 * Default Constructor.
	 */
	public TcpIpServer(EngineManager engineManager) {
		setName("TCP/IP Server");
		this.engineManager = engineManager;

		final int port = Config.getInstance().getServerPort();
		try {
			logger.info("Opening port {} for receive connections", port);
			serverSocket = new ServerSocket(port);

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

		//TODO: Every time 3 handshakes procedures fail within 60 seconds, the authentication service have to be disabled for 60 seconds.
		while (!Config.getInstance().isExit()) {
			try {

				//TODO: Check if IP is valid (in the range)

				// Connection respond
				pool.submit(new ClientConnection(serverSocket.accept(), engineManager));

			} catch (IOException e) {
				logger.error("Connection not accepted", e);
			}
		}

		pool.shutdownNow();
		logger.info("Server End");
	}
}
