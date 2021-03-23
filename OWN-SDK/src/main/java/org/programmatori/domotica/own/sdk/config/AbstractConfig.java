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
package org.programmatori.domotica.own.sdk.config;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Abstract configuration
 *
 * @since 16/10/2010
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 */
public abstract class AbstractConfig {
	private static final Logger logger = LoggerFactory.getLogger(AbstractConfig.class);

	public static final String DEFAULT_CONFIG_FOLDER = "conf";
	public static final String DEFAULT_CONFIG_PATH = "./" + DEFAULT_CONFIG_FOLDER;
	public static final String DEFAULT_CONFIG_FILE = "config.xml";
	//public static final String HOME_FILE = "home.config";

	private String configPath;
	private boolean configLoaded;

	private XMLConfiguration config = null;

	protected AbstractConfig() {
		this(new File(DEFAULT_CONFIG_PATH
			+ File.separator + DEFAULT_CONFIG_FILE));
	}

	protected AbstractConfig(File configFile) {
		//this.configPath = configPath;
		configLoaded = false;

		loadConfig(configFile);
	}

	/**
	 * Load from file
	 */
	private void loadConfig(File configFile) {
		if (configFile == null) return;
		if (!configFile.exists()) return;
		logger.debug("Config File: {}", configFile.getAbsolutePath());

		try {
			config = new XMLConfiguration();
			config.load(configFile);

			config.setThrowExceptionOnMissing(true);
			config.setAutoSave(true);

			configLoaded = true;
		} catch (ConfigurationException e) {
			logger.error("Error read from file", e);
		}
	}

	/**
	 * Load from Stream
	 */
	private void loadConfig(InputStream is) {
		if (is == null) return;

		try {
			config = new XMLConfiguration();
			config.load(is);

			config.setThrowExceptionOnMissing(true);
			config.setAutoSave(true);

			configLoaded = true;
		} catch (ConfigurationException e) {
			logger.error("Error read from stream", e);
		}
	}

	protected boolean isConfigLoaded() {
		return configLoaded;
	}

	public void setConfig(File configFile) {
		loadConfig(configFile);
	}

	public void setConfig(InputStream is) {
		if (is != null) {
			loadConfig(is);
		}
	}

	protected String getString(String key) {
		return config.getString(key);
	}

	protected String getString(String key, String defaultVaule) {
		return config.getString(key, defaultVaule);
	}

	protected Boolean getBoolean(String key) {
		return config.getBoolean(key);
	}

	protected Boolean getBoolean(String key, boolean defaultVaule) {
		return config.getBoolean(key, defaultVaule);
	}

	protected int getInt(String key, int defaultValue) {
		return config.getInt(key, defaultValue);
	}

	protected void setParam(String key, String value) {
		config.addProperty(key, value);
	}

	protected void setParamWithNameSearch(String nodeToSearch, String nameSearched, String param, String value) {
		int pos = indexOfAttributeName(nodeToSearch, nameSearched);

		if (pos > -1) {
			config.setProperty(nodeToSearch + "(" + pos + ")." + param, value);
			logger.debug("Unit: {} ({}).{}={}", nodeToSearch, pos, param, value);
		}
	}

	protected String getParamWithNameSearch(String nodeToSearch, String nameSearched, String param) {
		String value = null;
		int pos = indexOfAttributeName(nodeToSearch, nameSearched);

		if (pos > -1) {
			value = (String) config.getProperty(nodeToSearch + "(" + pos + ")." + param);
			logger.debug("Unit: {} ({}).{}={}", nodeToSearch, pos, param, value);
		}

		return value;
	}

	protected int indexOfAttributeName(String nodeToSearch, String nameSearched) {
		int idx = config.getMaxIndex(nodeToSearch);

		int pos = 0;
		while (!nameSearched.equals(config.getProperty(nodeToSearch + "(" + pos + ")[@name]")) && pos <= idx) {
			pos++;
		}

		if (nameSearched.equals(config.getProperty(nodeToSearch + "(" + pos + ")[@name]"))) {
			return pos;
		} else {
			return -1;
		}
	}

	public String getConfigPath() {
		String path = configPath;

		if (path == null) {
			try {
				String home = getHomeDirectory();
				path = home + File.separator + DEFAULT_CONFIG_FOLDER; // File.separator = "/"

			} catch (Exception e) {
				logger.error("Error in getConfigPath", e);
				path = DEFAULT_CONFIG_PATH;
			}
		}

		return path;
	}

	protected Map<String, String> getMap(String nodeToSearch) {
		int idx = config.getMaxIndex(nodeToSearch);

		Map<String, String> ret = new HashMap<>();

		int pos = 0;
		while (pos <= idx) {
			String key = (String) config.getProperty(nodeToSearch + "(" + pos + ")[@id]");
			String val = (String) config.getProperty(nodeToSearch + "(" + pos + ")");

			ret.put(key, val);

			pos++;
		}

		return ret;
	}

	protected void updateConfigFileVersion() {
		updateConfigFile(config);
	}

	protected abstract void updateConfigFile(XMLConfiguration config);

	/**
	 * Give the home of the project. <br>
	 * For return the home of the project need to have a file in the home
	 *
	 * @return path of home directory
	 */
	public static String getHomeDirectory() {
		File f = new File("");

		String filePath =  f.getAbsolutePath();


		return getPathFromString(filePath);
	}

	private static String getPathFromString(String path) {
		Logger logger = LoggerFactory.getLogger(AbstractConfig.class);

		String separator = File.separator;
		boolean first = true;
		StringBuilder home = new StringBuilder();

		logger.debug("Path {}", path);

		StringTokenizer st = new StringTokenizer(path, "/"); // URI Separator
		while (st.hasMoreTokens()) {
			String folder = st.nextToken();
			logger.debug("Foledr: {}", folder);

			boolean bFile = !(folder.equals("file:"));

			// BUG Linux starting slash
			if (separator.equals("/") && first) {
				folder = separator + folder;
				first = false;
			}

			// BUG Eclipse put in the bin
			boolean bBin = !(folder.equals("bin") && (st.countTokens() < 2));

			// BUG jar can not support . then use a real file for find a path
			boolean bRealFile = true; //TODO: fix me

			// BUG the home directory it cannot end with .jar
			boolean bJar = !(folder.endsWith(".jar!") && (st.countTokens() < 2));

			if (bBin && bRealFile && bJar && bFile) { // If i build under bin i don't insert in
				// home path
				if (home.length() > 0)
					home.append("/");
				home.append(folder);
				logger.debug("home: {}", home);
			}
		}

		return home.toString();
	}

}
