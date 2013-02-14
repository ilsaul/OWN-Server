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
package org.programmatori.domotica.own.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.utils.LogUtility;
import org.programmatori.domotica.own.server.engine.EngineManager;

public class Controller extends Thread {
	private static final Log log = LogFactory.getLog(Controller.class);
	
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
		
		System.out.println(Config.SERVER_NAME + " is Copyright (C) 2010-2012 Moreno Cattaneo");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY.");
		System.out.println("This is free software, and you are welcome to redistribute it");
		System.out.println("under certain conditions.");
		
		log.info(Config.SERVER_NAME + " is Copyright (C) 2010-2012 Moreno Cattaneo");
		log.info("This program comes with ABSOLUTELY NO WARRANTY.");
		log.info("This is free software, and you are welcome to redistribute it");
		log.info("under certain conditions.");
		log.info(Config.SERVER_NAME + " v." + Config.SERVER_VERSION + " Start");
	}
	
	@Override
	public void run() {
		try {
			while (!Config.getInstance().isExit()) {
				log.debug("Controller: control now");
				
				if (engine == null || engine.getState() == Thread.State.TERMINATED) {
					log.warn("Engine is down i load it");
					engine = new EngineManager();
					engine.start();
				}
				
				if (server == null || !server.isRunning()) {
					log.warn("Server is down i load it");
					server = new TcpIpServer(engine);
					Thread t = new Thread(server);
					t.start();
				}
				
				sleep(10000);
			}
		} catch (InterruptedException e) {
			log.error(LogUtility.getErrorTrace(e));
		}
		
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
		server.start();
	}
}
