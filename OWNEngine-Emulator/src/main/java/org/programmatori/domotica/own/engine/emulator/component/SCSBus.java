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
package org.programmatori.domotica.own.engine.emulator.component;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represent the real wire. Receive the message and delivery to all
 * component connected to the bus.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.2, 10/08/2016
 */
public class SCSBus extends ConfigBus {
	private static final long serialVersionUID = -7685595931533082563L;

	private static final Logger LOGGER = LoggerFactory.getLogger(SCSBus.class);

	private List<SCSComponent> components;
	private BlockingQueue<MsgBus> msgQueue;
	private boolean ready; // Tell if the bus is ready

	public SCSBus() {
		setName("SCS Bus");
		setDaemon(true);
		Config.getInstance().addThread(this);

		components = new ArrayList<SCSComponent>();
		msgQueue = new ArrayBlockingQueue<MsgBus>(1); // On the bus only 1 msg can go

		ready = true;
	}

	/**
	 * Add a new component to the bus.
	 *
	 * @param c new component to add
	 * @return if the bus have realy add the component
	 */
	@Override
	public boolean add(SCSComponent c) {
		boolean result = true;

		if (c == null || components.indexOf(c) > -1) {
			result = false;
		} else {
			components.add(c);
		}

		return result;
	}

	/**
	 * Send command to the bus
	 *
	 * @param msg is SCSMsg that need to send
	 */
	@Override
	public void sendCommand(SCSMsg msg, SCSComponent sender) {
		try {
			if (msg == null) throw new MessageFormatException("msg can't be empty");

			ready = false;
			LOGGER.debug("Msg Rx: {}", msg);
			MsgBus msgBus = new MsgBus(msg, sender);
			msgQueue.put(msgBus);
		} catch (Exception e) {
			LOGGER.error("Error:", e);
		}
	}

	@Override
	public void run() {
		while (!Config.getInstance().isExit()) {
			MsgBus msgBus = null;
			try {
				msgBus = msgQueue.take();
				LOGGER.debug("MSG Send To Component: {}", msgBus.getMsg().toString());
			} catch (InterruptedException e) {
				LOGGER.error("Error:", e);
				Thread.currentThread().interrupt();
			}

			notifyComponents(msgBus);
			ready = true;
		}
	}

	private void notifyComponents(MsgBus msgBus) {
		for (Iterator<SCSComponent> iter = components.iterator(); iter.hasNext();) {
			SCSComponent c = (SCSComponent) iter.next();

			if (!c.equals(msgBus.getComponent())) {
				c.reciveMessage(msgBus.getMsg());
				LOGGER.debug("Send to component: {}", c.toString());
			} else {
				LOGGER.debug("I don't send to sender");
			}

		}
	}

	public static void main(String[] args) {
		SCSBus emu = new SCSBus();
		emu.start();
		emu.loadConfig(Config.getInstance().getConfigPath() + "/emuNew.xml");

		// Create Light
//		SCSMsg lightStatus = new SCSMsg("*1*0*11##");
//		Light light = new Light(lightStatus);
//		emu.add(light);
//
//		lightStatus = new SCSMsg("*1*0*12##");
//		light = new Light(lightStatus);
//		emu.add(light);


		SCSMsg msg;
		try {
			msg = new SCSMsg("*#1*11##");
			emu.sendCommand(msg, null);

			msg = new SCSMsg("*#1*12##");
			emu.sendCommand(msg, null);

			msg = new SCSMsg("*1*1*0##");
			emu.sendCommand(msg, null);

			msg = new SCSMsg("*#1*11##");
			emu.sendCommand(msg, null);

			msg = new SCSMsg("*#1*12##");
			emu.sendCommand(msg, null);

			Config.getInstance().setExit(true);

		} catch (MessageFormatException e) {
			LOGGER.error("Error:", e);
		}
	}

	@Override
	public boolean isReady() {
		return ready;
	}
}
