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
package org.programmatori.domotica.own.plugin.map;

import org.programmatori.domotica.own.engine.emulator.component.Blind;
import org.programmatori.domotica.own.engine.emulator.component.Light;
import org.programmatori.domotica.own.sdk.component.SCSComponent;
import org.programmatori.domotica.own.sdk.component.Who;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.programmatori.domotica.own.sdk.server.engine.PlugIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class is the main part of the plug-in. This read all information about the building and display on the console.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0.2, 17/06/2013
 */
public class Map extends Thread implements PlugIn {
	private static final Logger logger = LoggerFactory.getLogger(Map.class);

	public static final String ASK_STATUS_LIGHT = "*#1*0##";
	public static final String ASK_STATUS_BLIND = "*#2*0##";

	private final EngineManager engine;
	private final java.util.Map<Integer, Set<SCSComponent>> localBus;
	private final int pauseStart;
	private final int pauseUnit;
	private int restartEvery;
	private final String fileName;

	private static class ComparatorPL implements Comparator<SCSComponent> {

		@Override
		public int compare(SCSComponent o1, SCSComponent o2) {
			Integer i1 = o1.getStatus().getWhere().getPL();
			Integer i2 = o2.getStatus().getWhere().getPL();
			return i1.compareTo(i2);
		}
	}

	public Map(EngineManager engine) {
		setName("SCS Map");
		setDaemon(true);

		// Retrieve Config Parameters
		pauseStart = Integer.parseInt(Config.getInstance().getNode("map.pause.start"));
		pauseUnit = Integer.parseInt(Config.getInstance().getNode("map.pause.unit"));
		fileName = Config.getInstance().getNode("map.file");
		restartEvery = Integer.parseInt(Config.getInstance().getNode("map.interval"));

		String unit = Config.getInstance().getNode("map.interval[@unit]");
		Time currentUnit = Time.createFromShortName(unit);

		// ATTENTION: Order is important
		switch (currentUnit) {
			case HOUR: restartEvery *= Time.HOUR.getUnderUnit();
			case MINUTE: restartEvery *= Time.MINUTE.getUnderUnit();
			case SECOND: restartEvery = restartEvery * Time.SECOND.getUnderUnit();
			break;

			default:
				throw new IllegalStateException("Unexpected value: " + currentUnit);
		}

		localBus = new TreeMap<>();
		this.engine = engine;
	}

	private void prepareLight() {
		try {
			SCSMsg msg = new SCSMsg(ASK_STATUS_LIGHT);

			engine.sendCommand(msg, this);
		} catch (MessageFormatException e) {
			logger.error("Error in prepareLight", e);
		}
	}

	private void prepareBlind() {
		try {
			SCSMsg msg = new SCSMsg(ASK_STATUS_BLIND);

			engine.sendCommand(msg, this);
		} catch (MessageFormatException e) {
			logger.error("Error in prepareBlind", e);
		}
	}

	private void addBus(Integer area, SCSComponent c) {
		Set<SCSComponent> room;
		if (localBus.containsKey(area)) {
			room = localBus.get(area);
		} else {
			room = new TreeSet<>(new ComparatorPL());
		}

		room.add(c);

		localBus.put(area, room);
	}

	@Override
	public void receiveMsg(SCSMsg msg) {
		Who who = Who.createByValue(msg.getWho().getMain());

		int iArea = msg.getWhere().getArea();
		String area = Integer.toString(iArea);
		String lightPoint = Integer.toString(msg.getWhere().getPL());
		String value = Integer.toString(msg.getWhat().getMain());

		switch (who) {

		case LIGHT:
			SCSComponent c = Light.create(null, area, lightPoint, value);
			addBus(iArea, c);
			break;

		case BLIND:
			c = Blind.create(null, area, lightPoint, value);
			addBus(iArea, c);
			break;

		default:
			// I don't manage other kind.
			break;
		}
	}

	@Override
	public long getId() {
		return -1;
	}

	@Override
	public void run() {

		while (true) {
			pause(pauseStart, 1);

			prepareLight();

			pause(pauseUnit, 2);

			prepareBlind();

			pause(pauseUnit, 3);

			if (localBus.size() == 0) {
				logger.info("Bus Empty");
			} else {
				createStatusFile(fileName);
				
				// Log the status
				for (java.util.Map.Entry<Integer, Set<SCSComponent>> area : localBus.entrySet()) {
					logger.info("Room: {} - {}", area.getKey(), Config.getInstance().getRoomName(area.getKey()));
					Set<SCSComponent> rooms = area.getValue();

					for (SCSComponent c : rooms) {
						logger.info("PL: {}({})", c.getStatus().getWhere().getPL(), Config.getInstance().getWhoDescription(c.getStatus().getWho().getMain()));
					}
				}
			}

			pause(restartEvery, 4);
		}
	}

	private void pause(int pause, int pos) {
		try {
			sleep(pause);

		} catch (InterruptedException e) {
			logger.error("error sleep {}: not important", pos, e);
			Thread.currentThread().interrupt();
		}
	}

	private void createStatusFile(String fileName) {
		StreamResult streamResult = new StreamResult(new File(fileName)); 
		
		SAXTransformerFactory  tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		TransformerHandler hd = null;

		try {
			hd = tf.newTransformerHandler();
		} catch (TransformerConfigurationException e) {
			logger.error("Error:", e);
		}

		if (hd != null) {
			Transformer serializer = hd.getTransformer();

			serializer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.ISO_8859_1.displayName()); // "ISO-8859-1"
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			//serializer.setOutputProperty( XalanOutputKeys.OUTPUT_PROP_INDENT_AMOUNT, "2" );

			hd.setResult(streamResult);
			try {
				hd.startDocument();

				AttributesImpl attrs = new AttributesImpl();
				hd.startElement("", "", "home", attrs);

				hd.startElement("", "", "version", attrs);
				hd.characters("2.0".toCharArray(), 0, 3);
				hd.endElement("", "", "version");

				attrs.clear();
				attrs.addAttribute("", "", "unit", "CDATA", "min");
				hd.startElement("", "", "statusSave", attrs);
				hd.characters("10".toCharArray(), 0, 2);
				hd.endElement("", "", "statusSave");

				// ----------------------------------------- Area
				for (Integer area : localBus.keySet()) {
					attrs.clear();
					attrs.addAttribute("", "", "id", "CDATA", area.toString());
					attrs.addAttribute("", "", "name", "CDATA", Config.getInstance().getRoomName(area));
					hd.startElement("", "", "area", attrs);

					// ----------------------------------------- Component
					Set<SCSComponent> rooms = localBus.get(area);
					for (SCSComponent c : rooms) {
						logger.info("PL: {}({})", c.getStatus().getWhere().getPL(), Config.getInstance().getWhoDescription(c.getStatus().getWho().getMain()));

						attrs.clear();
						attrs.addAttribute("", "", "type", "CDATA", Config.getInstance().getWhoDescription(c.getStatus().getWho().getMain()));
						attrs.addAttribute("", "", "pl", "CDATA", Integer.toString(c.getStatus().getWhere().getPL()));
						hd.startElement("", "", "component", attrs);
						hd.characters("0".toCharArray(), 0, 1);
						hd.endElement("", "", "component");
					}
					// ----------------------------------------- Component

					hd.endElement("", "", "area");
				}
				// ----------------------------------------- End Area

				// ----------------------------------------- Scheduler
				attrs.clear();
				hd.startElement("", "", "scheduler", attrs);

				attrs.clear();
				attrs.addAttribute("", "", "time", "CDATA", "-1");
				hd.startElement("", "", "command2", attrs);
				hd.characters("*1*1*11##".toCharArray(), 0, 9);
				hd.endElement("", "", "command2");

				hd.endElement("", "", "scheduler");
				// ----------------------------------------- End Scheduler

				hd.endElement("", "", "home");
				hd.endDocument();
			} catch (SAXException e) {
				logger.error("Conversion is in error", e);
			}
		}
	}
	
	public static void main(String[] args) {
		Map map = new Map(null);
		
		map.createStatusFile("test.xml");
	}
}
