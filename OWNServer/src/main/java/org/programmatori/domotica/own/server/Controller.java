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
import org.programmatori.domotica.own.server.engine.EngineManagerImpl;

/**
 * Launcher for OWN Server
 * 
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.1.1, 22/02/2013
 * @since OWNServer 0.5.0
 */
public class Controller extends Thread {
	private static final Log log = LogFactory.getLog(Controller.class);
	
	private EngineManagerImpl engine;
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
		
		String line1 = Config.SERVER_NAME + " is Copyright (C) 2010-2013 Moreno Cattaneo";
		String line2 = "This program comes with ABSOLUTELY NO WARRANTY.";
		String line3 = "This is free software, and you are welcome to redistribute it";
		String line4 = "under certain conditions.";
		
		System.out.println(line1);
		System.out.println(line2);
		System.out.println(line3);
		System.out.println(line4);
		
		log.info(line1);
		log.info(line2);
		log.info(line3);
		log.info(line4);
		log.info(Config.SERVER_NAME + " v." + Config.SERVER_VERSION + " Start");
	}
	
	@Override
	public void run() {
		try {
			while (!Config.getInstance().isExit()) {
				log.debug("Controller: control now");
				
				if (engine == null || engine.getState() == Thread.State.TERMINATED) {
					log.warn("Engine is down i load it");
					engine = new EngineManagerImpl();
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
