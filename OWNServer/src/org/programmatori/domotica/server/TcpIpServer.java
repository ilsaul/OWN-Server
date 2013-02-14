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
package org.programmatori.domotica.server;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.server.engine.EngineManager;

/**
 * This is the main class for start OWNServer. This class is 
 * designed to received Tcp/Ip connections and sort them in new 
 * {@link ClientConnection} create for manage them. 
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.2.1, 16/10/2010
 * @since OWNServer v0.1.0
 */
public class TcpIpServer implements Runnable {
	private static final Log log = LogFactory.getLog(TcpIpServer.class);

	private ServerSocket serverSocket;
	private ClientList list;
	private EngineManager engine;
	private int maxConnections;

	/**
	 * Default Constructor
	 */
	public TcpIpServer(EngineManager engine) {
		Thread.currentThread().setName("TCP/IP Server");
		Config.getInstance().addThread(Thread.currentThread());

		this.engine = engine;
		
		list = new ClientList();

		int port = Config.getInstance().getServerPort();
		try {
			log.info("Start listen on port: " + port);
			serverSocket = new ServerSocket(port);

		} catch (IOException e) {
			log.error("Could not listen on port: " + port);
			System.exit(-1);
		}

		maxConnections = Config.getInstance().getMaxConnections();
		log.debug("Max Connections: " + maxConnections);

	}

	@Override
	public void run() {
		while (!Config.getInstance().isExit()) {
			try {
				if (list.getSize() < maxConnections) {
					log.info("Client Connecting ... (N°" + list.getSize() + ")");
					
					// Connection respond
					ClientConnection connection = new ClientConnection(serverSocket.accept(), this, engine);
					int size = list.add(connection);
					
					String name = "Conn #" + connection.getId();
					log.debug("Connection " + size + ": '" + name + "'");
					Thread t = new Thread(connection, name);
					t.start();
				} else {
					log.warn("Maximum number of connection reached " + maxConnections);
				}
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		log.info("Server End");
	}
	
	public boolean isRunning() {
		return Thread.currentThread().getState() != Thread.State.TERMINATED;
	}

	public void remove(ClientConnection client) {
		list.remove(client);
	}
}
