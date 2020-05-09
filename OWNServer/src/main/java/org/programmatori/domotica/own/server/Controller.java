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

/**
 * Launcher for OWN Server
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.1.1, 22/02/2013
 * @since OWNServer 0.5.0
 */
public class Controller implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Controller.class);

	private EngineManager engine;
	private TcpIpServer server;

	/**
	 * Default Constructor
	 */
	public Controller() {
		this(null);
	}

	public Controller(String configFile) {
		Thread.currentThread().setName("Controller I");
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
	}

	@Override
	public void run() {
		try {
			while (!Config.getInstance().isExit()) {
				logger.debug("Controller: control now");

				if (engine == null || engine.getState() == Thread.State.TERMINATED) {
					logger.warn("Engine is down, I load it");
					engine = new EngineManagerImpl(); //TODO: Read from configuration (exist only one)
					engine.start();
				}

				if (server == null || !server.isRunning()) {
					logger.warn("Server is down, I load it");
					server = new TcpIpServer(engine);
					Thread t = new Thread(server);
					t.start();
				}

				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			logger.error("Error:", e);
		}
	}

	/**
	 * For test if the server continue to work. It's use in junit test.
	 * @return
	 */
	public boolean isAliveServer() {
		return server.isRunning();
	}

	/**
	 * For start the Server
	 */
	public static void main(String[] args) {
		// Load configuration
		String configFile = null;
		if (args.length > 0) {
			configFile = args[0];
		}

		Controller server = new Controller(configFile);
		server.run();
	}
}
