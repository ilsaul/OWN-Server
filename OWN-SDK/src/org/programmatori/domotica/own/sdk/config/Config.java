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
package org.programmatori.domotica.own.sdk.config;

import java.util.*;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.programmatori.domotica.own.sdk.utils.LogUtility;
import org.programmatori.domotica.own.sdk.utils.TimeUtility;

/**
 * Configuration
 * @version 1.2 19/03/2012
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 */
public class Config extends AbstractConfig {
	private static Log log = LogFactory.getLog(Config.class);
	private static Config instance = null;
	public static final String SERVER_VERSION = "0.4.2";
	public static final String SERVER_NAME = "OWN Server";

	private boolean exit = false; // Tell to the application if it need to shutdown
	private List<Thread> listThread;
	private Calendar startTime;
	private long timeDiff;
	private TimeZone timeZone;

	private Config() {
		super();
		
		startTime = GregorianCalendar.getInstance();

		DOMConfigurator.configure(getConfigPath() + "/log4j.xml");
		//DOMConfigurator.configureAndWatch(getConfigPath() + "/log4j.xml");
		//Log log2 = LogFactory.getLog(Config.class);
		//Logger log3 = Logger.getLogger(Config.class);
		//log3.error("Config Create");
		//log2.error("Config Create");
		
		if (isConfigLoaded()) {
			// Check File Version to update information
			String version = getString("version");
			log.debug("config file version: " + version);
			if (version == null || !version.equals(SERVER_VERSION)) {
				updateConfigFileVersion();
			}
		}

		listThread = new ArrayList<Thread>();
	}

	public static Config getInstance() {
		if (instance == null) {
			synchronized (Config.class) {
				if (instance == null) {
					instance = new Config();
				}
			}
		}

		return instance;
	}

	public boolean isDebug() {
		return false;
	}

	@Override
	protected void updateConfigFile(XMLConfiguration config) {

		// Last change is the server version
		String version = getString("version");
		if (version == null) {
			setParam("version", SERVER_VERSION);
		}
	}

	public boolean isExit() {
		return exit;
	}

	public void setExit(boolean exit) {
		this.exit = exit;

		KillThread kill = new KillThread(listThread);
		kill.start();
	}

	public int getServerPort() {
		return getInt("server.port", 20000);
	}

	public int getMaxConnections() {
		return getInt("server.maxConnections", 50);
	}

	public int getSendTimeout() {
		return getInt("server.timeoutSend", 4000);
	}

	/**
	 * BTicino say if not connect in 30s than disconnect the client
	 */
	public int getWelcomeTimeout() {
		return getInt("server.timeoutWelcome", 30000);
	}

	public String getBus() {
		return getString("bus", "org.programmatori.domotica.bticino.bus.L4686Sdk");
	}

//	public String getL4686Sdk() {
//		return getString("l4686sdk", "COM6");
//	}
	
	public String getNode(String nodeName) {
		return getString(nodeName);
	}

	public String getRoomName(int area) {
		Map<String, String> rooms = getMap("areas.area");

		return rooms.get(Integer.toString(area));
	}

	public String getWhoDescription(int who) {
		String desc = null;

		
		try {
			ResourceBundle resource = ResourceBundle.getBundle("Who");
			desc = resource.getString("" + who);
		} catch (Exception e) {
			log.error(LogUtility.getErrorTrace(e));
			desc = "" + who;
		}

		return desc;
	}

	/**
	 * For killing purpose
	 */
	public synchronized void addThread(Thread t) {
		listThread.add(t);
	}

	public List<String> getPlugIn() {
		Map<String, String> plugins = getMap("plugins.plugin");

		return new ArrayList<String>(plugins.values());
	}
	
	public Calendar getStartUpTime() {
		return startTime;
	}

	/**
	 * Let you set the user time
	 */
	public void setUserTime(Calendar userTime) {
		Calendar now = GregorianCalendar.getInstance();
		
		timeDiff = TimeUtility.timeDiff(userTime, now);
		timeZone = userTime.getTimeZone();
	}
	
	public Calendar getCurentTime() {
		Calendar now = GregorianCalendar.getInstance();
		
		Calendar ret = TimeUtility.timeAdd(timeDiff, now);
		ret.setTimeZone(timeZone);
		return ret;
	}
}
