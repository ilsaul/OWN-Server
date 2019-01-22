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
package org.programmatori.domotica.own.sdk.config;

import java.io.File;
import java.util.*;

import org.apache.commons.configuration.XMLConfiguration;
import org.programmatori.domotica.own.sdk.msg.MessageBusLog;
import org.programmatori.domotica.own.sdk.utils.TimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * Configuration
 *
 * @version 1.3, 10/08/2015
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 */
public class Config extends AbstractConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
	public static final String SERVER_VERSION = "0.5.3";
	public static final String SERVER_NAME = "OWN Server";

	private static Config instance = null;

	private boolean exit = false; // Tell to the application if it need to shutdown
	private List<Thread> listThread;
	private Calendar startTime;
	private long timeDiff;
	private TimeZone timeZone;
	private MessageBusLog messageLog;

	private Config() {
		super();

		startTime = GregorianCalendar.getInstance();

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(context);
		context.reset(); // override default configuration
		// inject the name of the current application as "application-name"
		// property of the LoggerContext
		context.putProperty("application-name", "OWNServer");
		try {
			String cfg = getConfigPath() + File.separatorChar + "logback.xml";
			System.out.println("Log path: " + cfg);
			jc.doConfigure(cfg);
		} catch (Exception e) { // if is logback, error is JoranException
			e.printStackTrace();
		}

		if (isConfigLoaded()) {
			// Check File Version to update information
			String version = getString("version");
			LOGGER.debug("config file version: {}", version);
			if (version == null || !version.equals(SERVER_VERSION)) {
				updateConfigFileVersion();
			}
		}

		listThread = new ArrayList<Thread>();

		messageLog = new MessageBusLog();
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
			LOGGER.error("Errore:", e); //LogUtility.getErrorTrace(e));
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

		Calendar ret;
		if (timeDiff != 0) {
			ret = TimeUtility.timeAdd(timeDiff, now);
		} else {
			ret = now;
		}

		if (timeZone == null) {
			timeZone = TimeZone.getDefault();
		}
		ret.setTimeZone(timeZone);

		return ret;
	}

	public MessageBusLog getMessageLog() {
		return messageLog;
	}
}
