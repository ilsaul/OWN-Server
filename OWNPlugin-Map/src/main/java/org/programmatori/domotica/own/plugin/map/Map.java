/*
 * OWN Server is
 * Copyright (C) 2010-2013 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.programmatori.domotica.own.emulator.Blind;
import org.programmatori.domotica.own.emulator.Light;
import org.programmatori.domotica.own.emulator.SCSComponent;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.programmatori.domotica.own.sdk.server.engine.PlugIn;
import org.programmatori.domotica.own.sdk.utils.LogUtility;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class is the main part of the plug-in. This read all information about the building and display on the console.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0.2, 17/06/2013
 */
public class Map extends Thread implements PlugIn {
	private static final Log log = LogFactory.getLog(Map.class);

	private EngineManager engine;
	private java.util.Map<Integer, Set<SCSComponent>> localBus;
	private int pauseStart;
	private int pauseUnit;
	private int restartEvery;
	private String fileName;

	private class comparatorPL implements Comparator<SCSComponent> {

		@Override
		public int compare(SCSComponent o1, SCSComponent o2) {
			Integer i1 = new Integer(o1.getStatus().getWhere().getPL());
			Integer i2 = new Integer(o2.getStatus().getWhere().getPL());
			return i1.compareTo(i2);
		}
	}

	public Map(EngineManager engine) {
		setName("SCS Map");
		setDaemon(true);

		// Retrive Config Parameters
		pauseStart = Integer.parseInt(Config.getInstance().getNode("map.pause.start"));
		pauseUnit = Integer.parseInt(Config.getInstance().getNode("map.pause.unit"));
		fileName = Config.getInstance().getNode("file");
		restartEvery = Integer.parseInt(Config.getInstance().getNode("map.intervall"));

		String unit = Config.getInstance().getNode("map.intervall[@unit]");
		if ("min".equals(unit)) {
			restartEvery = restartEvery * 60 * 1000;
		}
		if ("sec".equals(unit)) {
			restartEvery = restartEvery * 1000;
		}
		if ("hour".startsWith(unit)) {
			restartEvery = restartEvery * 60 * 60 * 1000;
		}


		localBus = new TreeMap<Integer, Set<SCSComponent>>();
		this.engine = engine;

//		try {
//			engine.addMonitor(this);
//		} catch (Exception e) {
//			// stub
//		}
	}

	private void prepareLight() {
		try {
			SCSMsg msg = new SCSMsg("*#1*0##");

			engine.sendCommand(msg, this);
		} catch (MessageFormatException e) {
			log.error(LogUtility.getErrorTrace(e));
		}
	}

	private void prepareBlind() {
		try {
			SCSMsg msg = new SCSMsg("*#2*0##");

			engine.sendCommand(msg, this);
		} catch (MessageFormatException e) {
			log.error(LogUtility.getErrorTrace(e));
		}
	}

	private void addBus(Integer area, SCSComponent c) {
		Set<SCSComponent> room = null;
		if (localBus.containsKey(area)) {
			room = localBus.get(area);
		} else {
			room = new TreeSet<SCSComponent>(new comparatorPL());
		}

		room.add(c);

		localBus.put(area, room);
	}

	@Override
	public void reciveMsg(SCSMsg msg) {
		switch (msg.getWho().getMain()) {
		case Light.MUST_WHO: //TODO: Remove dependance from Emulator
			int area = msg.getWhere().getArea();
			int lightPoint = msg.getWhere().getPL();
			int value = msg.getWhat().getMain();
			SCSComponent c = Light.create(null, "" + area, "" + lightPoint, "" + value);
			addBus(area, c);
			break;

		case Blind.MUST_WHO:
			area = msg.getWhere().getArea();
			lightPoint = msg.getWhere().getPL();
			value = msg.getWhat().getMain();
			c = Blind.create(null, "" + area, "" + lightPoint, "" + value);
			addBus(area, c);
			break;

		default:
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
			try {
				sleep(pauseStart);
			} catch (InterruptedException e) {
				log.error(LogUtility.getErrorTrace(e));
			}
			prepareLight();
			try {
				sleep(pauseUnit);
			} catch (InterruptedException e) {
				log.error(LogUtility.getErrorTrace(e));
			}
			prepareBlind();
			try {
				sleep(pauseUnit);
			} catch (InterruptedException e) {
				log.error(LogUtility.getErrorTrace(e));
			}

			if (localBus.size() == 0) {
				log.info("Bus Empty");
			} else {
				createStatusFile(fileName);

				// Log the status
				for (Iterator<Integer> iterAree = localBus.keySet().iterator(); iterAree.hasNext();) {
					Integer area = (Integer) iterAree.next();

					log.info("Room: " + area + " - " + Config.getInstance().getRoomName(area));
					Set<SCSComponent> rooms = localBus.get(area);
					for (Iterator<SCSComponent> iterRoom = rooms.iterator(); iterRoom.hasNext();) {
						SCSComponent c = (SCSComponent) iterRoom.next();
						log.info("PL: " + c.getStatus().getWhere().getPL() + "(" + Config.getInstance().getWhoDescription(c.getStatus().getWho().getMain()) + ")");
					}
				}
			}

			try {
				wait(restartEvery);
			} catch (Exception e) {
				// stub!
			}
		}
	}

	private void createStatusFile(String fileName) {
		StreamResult streamResult = new StreamResult(new File(fileName));

		SAXTransformerFactory  tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		TransformerHandler hd = null;
		try {
			hd = tf.newTransformerHandler();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		Transformer serializer = hd.getTransformer();

		serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
		//serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "users.dtd");
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
			attrs.addAttribute("","","unit","CDATA", "min");
			hd.startElement("", "", "statusSave", attrs);
			hd.characters("10".toCharArray(), 0, 2);
			hd.endElement("", "", "statusSave");

			// ----------------------------------------- Area
			for (Iterator<Integer> iterAree = localBus.keySet().iterator(); iterAree.hasNext();) {
				Integer area = (Integer) iterAree.next();

				attrs.clear();
				attrs.addAttribute("","","id","CDATA", area.toString());
				attrs.addAttribute("","","name","CDATA", Config.getInstance().getRoomName(area));
				hd.startElement("", "", "area", attrs);

				// ----------------------------------------- Component
				Set<SCSComponent> rooms = localBus.get(area);
				for (Iterator<SCSComponent> iterRoom = rooms.iterator(); iterRoom.hasNext();) {
					SCSComponent c = (SCSComponent) iterRoom.next();
					log.info("PL: " + c.getStatus().getWhere().getPL() + "(" + Config.getInstance().getWhoDescription(c.getStatus().getWho().getMain()) + ")");

					attrs.clear();
					attrs.addAttribute("","","type","CDATA", Config.getInstance().getWhoDescription(c.getStatus().getWho().getMain()));
					attrs.addAttribute("","","pl","CDATA",  "" + c.getStatus().getWhere().getPL());
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
			attrs.addAttribute("","","time","CDATA", "-1");
			hd.startElement("", "", "command2", attrs);
			hd.characters("*1*1*11##".toCharArray(), 0, 9);
			hd.endElement("", "", "command2");

			hd.endElement("", "", "scheduler");
			// ----------------------------------------- End Scheduler

			hd.endElement("", "", "home");
			hd.endDocument();
		} catch (SAXException e) {
			e.printStackTrace();
		}





//		for (Iterator<Integer> iterAree = localBus.keySet().iterator(); iterAree.hasNext();) {
//			Integer area = (Integer) iterAree.next();
//
//			log.info("Room: " + area + " - " + Config.getInstance().getRoomName(area));
//			Set<SCSComponent> rooms = localBus.get(area);
//			for (Iterator<SCSComponent> iterRoom = rooms.iterator(); iterRoom.hasNext();) {
//				SCSComponent c = (SCSComponent) iterRoom.next();
//				log.info("PL: " + c.getStatus().getWhere().getPL() + "(" + Config.getInstance().getWhoDescription(c.getStatus().getWho().getMain()) + ")");
//			}
//		}
	}

	public static void main(String[] args) {
		Map map = new Map(null);

		map.createStatusFile("test.xml");
	}
}
