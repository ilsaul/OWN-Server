/*
 * Copyright (C) 2010-2019 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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

import net.jcip.annotations.ThreadSafe;
import org.apache.commons.configuration.XMLConfiguration;
import org.programmatori.domotica.own.sdk.msg.MessageBusLog;
import org.programmatori.domotica.own.sdk.utils.TimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * Configuration
 *
 * @since 10/08/2015
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 */
@ThreadSafe
public class Config extends AbstractConfig {
	private final Logger logger = LoggerFactory.getLogger(Config.class);

	public static final String SERVER_VERSION = "0.4.8";
	public static final String SERVER_NAME = "OWN Server";

	private static final String VERSION = "version";

	private static Config instance = null;

	private boolean exit = false; // Tell to the application if it need to shutdown
	private List<Thread> listThread;
	private Calendar startTime;
	private long timeDiff;
	private TimeZone timeZone;
	private MessageBusLog messageLog;

	private Config() {
		super();

		startTime = Calendar.getInstance();

		if (isConfigLoaded()) {
			// Check File Version to update information
			String version = getString(VERSION);
			logger.debug("config file version: {}", version);
			if (version == null || !version.equals(SERVER_VERSION)) {
				updateConfigFileVersion();
			}
		}

		listThread = new ArrayList<>();

		messageLog = new MessageBusLog();
	}

	public static synchronized Config getInstance() {
		if (instance == null) {
			instance = new Config();
		}

		return instance;
	}

	public boolean isDebug() {
		return false;
	}

	@Override
	protected void updateConfigFile(XMLConfiguration config) {

		// Last change is the server version
		String version = getString(VERSION);
		if (version == null) {
			setParam(VERSION, SERVER_VERSION);
		}
	}

	public boolean isExit() {
		return exit;
	}

	public void setExit(boolean exit) {
		this.exit = exit;
		logger.info("Quit Server");

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
			logger.error("Error:", e);
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

		return new ArrayList<>(plugins.values());
	}

	public Calendar getStartUpTime() {
		return startTime;
	}

	/**
	 * Let you set the user time
	 */
	public void setUserTime(Calendar userTime) {
		Calendar now = Calendar.getInstance();

		timeDiff = TimeUtility.timeDiff(userTime, now);
		timeZone = userTime.getTimeZone();
	}

	public Calendar getCurentTime() {
		Calendar now = Calendar.getInstance();

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
