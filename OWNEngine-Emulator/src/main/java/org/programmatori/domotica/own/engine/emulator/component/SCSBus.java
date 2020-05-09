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
package org.programmatori.domotica.own.engine.emulator.component;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.programmatori.domotica.own.sdk.component.SCSComponent;
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

	private static final Logger logger = LoggerFactory.getLogger(SCSBus.class);

	private final List<SCSComponent> components;
	private final BlockingQueue<MsgBus> msgQueue;
	private boolean ready; // Tell if the bus is ready

	public SCSBus() {
		setName("SCS Bus");
		setDaemon(true);
		Config.getInstance().addThread(this);

		components = new ArrayList<>();
		msgQueue = new ArrayBlockingQueue<>(1); // On the bus only 1 msg can go

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

		if (c == null || components.contains(c)) {
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
			logger.debug("Msg Rx: {}", msg);
			MsgBus msgBus = new MsgBus(msg, sender);
			msgQueue.put(msgBus);
		} catch (Exception e) {
			logger.error("Error sendCommand:", e);
		}
	}

	@Override
	public void run() {
		while (!Config.getInstance().isExit()) {
			MsgBus msgBus = null;
			try {
				msgBus = msgQueue.take();
				logger.debug("MSG Send To Component: {}", msgBus.getMsg());
			} catch (InterruptedException e) {
				logger.error("Error run:", e);
				Thread.currentThread().interrupt();
			}

			try {
				notifyComponents(msgBus);
			} catch (NullPointerException e) {
				logger.error("error on notification", e);
			}
			ready = true;
		}
	}

	private void notifyComponents(MsgBus msgBus) {
		for (SCSComponent c : components) {
			if (!c.equals(msgBus.getComponent())) {
				c.receiveMessage(msgBus.getMsg());
				logger.debug("Send to component: {}", c);
			} else {
				logger.debug("I don't send to sender");
			}

		}
	}

	public static void main(String[] args) {
		SCSBus emu = new SCSBus();
		emu.start();
		emu.loadConfig(Config.getInstance().getConfigPath() + "/emuNew.xml");

		// Create Light

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
			logger.error("Error main:", e);
		}
	}

	@Override
	public boolean isReady() {
		return ready;
	}
}
