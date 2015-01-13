/*
 * OWN Server is
 * Copyright (C) 2010-2015 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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
package org.programmatori.domotica.own.emulator;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since TCPIPServer v1.1.0
 * @version 0.2, 29/06/2011
 */
public abstract class ConfigBus extends Thread implements Bus {
	private static final Logger logger = LoggerFactory.getLogger(ConfigBus.class);

	private boolean save; //Save the configuration in the config file

	public ConfigBus() {
		save = false;
		setDaemon(true);
	}

	abstract public boolean add(SCSComponent c);

	public void loadConfig(String fileName) {
		try {
			XMLConfiguration config = new XMLConfiguration(fileName);

			String version = config.getString("version");

			if (version.equals("1.0")) {
				loadConfig10(config);
			} else if (version.equals("2.0")) {
				loadConfig20(config);
			} else {
				logger.warn("Unknown version of the configuration bus: {}", version);
			}

		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

	}

	private void loadConfig20(XMLConfiguration config) {
		if (config.getInt("statusSave", 0) == 1) {
			config.setAutoSave(save);
		}

		int pos = 0;
		List<?> areas = config.configurationsAt("area");
		for (Iterator<?> iter = areas.iterator(); iter.hasNext();) {
			HierarchicalConfiguration areaConf = (HierarchicalConfiguration ) iter.next();
			String area = config.getString("area(" + pos + ")[@id]");

			int posC = 0;
			List<?> components = areaConf.getList("component");
			for (Iterator<?> iterC = components.iterator(); iterC.hasNext();) {
				String value = (String) iterC.next();

				String type = areaConf.getString("component(" + posC + ")[@type]");
				String lightPoint = areaConf.getString("component(" + posC + ")[@pl]");

				SCSComponent c = null;
				if (type.equals("light")) {
					c = Light.create(this, area, lightPoint, value);
				} else if (type.equals("blind")) {
					c = Blind.create(this, area, lightPoint, value);
				}  else if (type.equals("power")) {
					c = PowerUnit.create(this, area, value);
				}
				add(c);
				if (c instanceof Thread) {
					Thread t = (Thread) c;
					t.start();
				}
				posC++;
			}


//			int posC = 0;
//			List<?> components = config.configurationsAt("component");
//			for (Iterator<?> iterC = components.iterator(); iter.hasNext();) {
//				HierarchicalConfiguration component = (HierarchicalConfiguration ) iter.next();
//
//				String type = areaConf.getString("component(" + posC + ")[@type]");
//				String lightPoint = areaConf.getString("component(" + posC + ")[@pl]");
//
//				String value = component.getString("value");

			pos++;
		}
	}

	private void loadConfig10(XMLConfiguration config) {
		if (config.getInt("statusSave", 0) == 1) {
			config.setAutoSave(save);
		}

		int pos = 0;
		List<?> components = config.configurationsAt("component");
		for (Iterator<?> iter = components.iterator(); iter.hasNext();) {
			HierarchicalConfiguration component = (HierarchicalConfiguration ) iter.next();

			String type = config.getString("component(" + pos + ")[@type]");
			String area = component.getString("area");
			String lightPoint = component.getString("lightPoint");
			String value = component.getString("value");

			SCSComponent c = null;
			if (type.equals("light")) {
				c = Light.create(this, area, lightPoint, value);
			} else if (type.equals("blind")) {
				c = Blind.create(this, area, lightPoint, value);
			}
			add(c);
			if (c instanceof Thread) {
				Thread t = (Thread) c;
				t.start();
			}

			pos++;
		}
	}

	public void setSave(boolean save) {
		this.save = save;
	}
}
