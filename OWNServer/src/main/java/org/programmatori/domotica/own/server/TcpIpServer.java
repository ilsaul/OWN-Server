/*
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
package org.programmatori.domotica.own.server;

import java.io.IOException;
import java.net.ServerSocket;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wait the network connection. It's
 * designed to received Tcp/Ip connections and send them to
 * {@link ClientConnection} for the management.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.2.1, 16/10/2010
 * @since OWNServer v0.1.0
 */
public class TcpIpServer implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpIpServer.class);

	private ServerSocket serverSocket;
	private ClientList list;
	private EngineManager engine;
	private int maxConnections;

	/**
	 * Default Constructor.
	 */
	public TcpIpServer(EngineManager engine) {
		Thread.currentThread().setName("TCP/IP Server");
		Config.getInstance().addThread(Thread.currentThread());

		this.engine = engine;

		this.list = new ClientList();

		final int port = Config.getInstance().getServerPort();
		try {
			LOGGER.info("Start listen on port: {}", port);
			this.serverSocket = new ServerSocket(port);

		} catch (IOException e) {
			LOGGER.error("Could not listen on port: {}", port);
			System.exit(-1);
		}

		this.maxConnections = Config.getInstance().getMaxConnections();
		LOGGER.debug("Max Connections: {}", this.maxConnections);
	}

	/**
	 * Main Loop of the server.
	 * This loop acept connection from the client.
	 */
	@Override
	public void run() {
		while (!Config.getInstance().isExit()) {
			try {
				int size = this.list.getSize();
				if (size < this.maxConnections) {
					LOGGER.info("Client Connecting ... (Nº{})", size);

					// Connection respond
					ClientConnection connection = new ClientConnection(this.serverSocket.accept(), this, this.engine);
					size = this.list.add(connection);

					final String name = "Conn #" + connection.getId();
					LOGGER.debug("Connection {}: '{}'", size, name);
					Thread t = new Thread(connection, name);
					t.start();
				} else {
					LOGGER.warn("Maximum number of connection reached {}", this.maxConnections);
				}

			} catch (IOException e) {
				LOGGER.error("Connection not accepted", e);
				//e.printStackTrace();
			}
		}

		LOGGER.info("Server End");
	}

	/**
	 * Check if the server still running.
	 * @return true if the server is running
	 */
	public boolean isRunning() {
		return Thread.currentThread().getState() != Thread.State.TERMINATED;
	}

	/**
	 * Remove a client from the list.
	 * @param client that need to be removed
	 */
	public void remove(ClientConnection client) {
		this.list.remove(client);
	}
}
