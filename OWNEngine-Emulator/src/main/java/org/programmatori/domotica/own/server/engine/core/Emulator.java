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
package org.programmatori.domotica.own.server.engine.core;

import java.util.*;

import org.programmatori.domotica.own.emulator.SCSBus;
import org.programmatori.domotica.own.emulator.SCSComponent;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.SCSEvent;
import org.programmatori.domotica.own.sdk.server.engine.SCSListener;
import org.programmatori.domotica.own.sdk.server.engine.core.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an Engine emulator. This create a fake BUS for testing.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.8.1, 13/01/2015
 */
public class Emulator implements Engine, SCSComponent {
	private static final long serialVersionUID = -4985192779288083426L;

	private static final Logger logger = LoggerFactory.getLogger(Emulator.class);

	private ArrayList<SCSListener> listListener = null;
	private SCSBus bus;

	/**
	 * Default Constructor. This constructor load the configuration from a File.
	 */
	public Emulator()  {
		listListener = new ArrayList<SCSListener>();
		bus = new SCSBus();
		bus.start();

		bus.loadConfig(Config.getInstance().getConfigPath() + "/emuNew.xml");
		bus.add(this);
	}

	@Override
	public void sendCommand(SCSMsg msg) {
		logger.trace("Start sendCommand");
		//SCSMsg msgOut =
		bus.sendCommand(msg, this);

//		if (!msg.equals(SCSMsg.MSG_ACK))
//		  reciveMessage(bus, msgOut);

		logger.trace("End sendCommand");
	}

	@Override
	public void addEventListener(SCSListener listener) throws TooManyListenersException {
		listListener.add(listener);
	}

	@Override
	public void removeEventListener(SCSListener listener) {
		listListener.remove(listener);
	}

	@Override
	public void reciveMessage(SCSMsg msg) {
		logger.debug("Arrive from Bus Emulator: {}", msg);

		SCSEvent event = new SCSEvent(this, msg);
		notifyListeners(event);
	}

	private void notifyListeners(SCSEvent event) {
		for (SCSListener listener: listListener)
            listener.SCSValueChanged(event);
	}

	@Override
	public boolean isReady() {
		return bus.isReady();
	}

	/**
	 * Testing Purpose
	 */
	public static void main(String[] args) {
		try {
			Emulator e = new Emulator();
			SCSMsg m = new SCSMsg("*#1*0##");
			e.sendCommand(m);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public SCSMsg getStatus() {
		return null;
	}
}
