/*
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

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SCSBaseComponent extends Thread implements SCSComponent, Serializable {
	private static final long serialVersionUID = -4174453941105833387L;

	private static final Logger logger = LoggerFactory.getLogger(SCSBaseComponent.class);

	private Who who;
	private What what;
	private Where where; // they are 2 digits A + PL
	private int group;
	private Property property;
	private Value value;

	private BlockingQueue<SCSMsg> msgOut;
	private Bus bus;

	public SCSBaseComponent(SCSMsg msg, Bus bus) {
		super();
		setDaemon(true);

		who = msg.getWho();
		what = msg.getWhat();
		where = msg.getWhere();
		property = msg.getProperty();
		value = msg.getValue();

		if (what == null) what = new What("0");

		setName(Config.getInstance().getWhoDescription(getWho().getMain()) + " Where: " + getWhere());
		Config.getInstance().addThread(this);

		msgOut = new ArrayBlockingQueue<SCSMsg>(1);
		this.bus = bus;
		group = -1;
	}

	protected Who getWho() {
		return who;
	}

	protected What getWhat() {
		return what;
	}

	protected Where getWhere() {
		return where;
	}

	protected Property getpProperty() {
		return property;
	}

	public Value getValue() {
		return value;
	}

	public void setWhat(What what) {
		this.what = what;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	protected boolean isMyMsg(SCSMsg msg) {
		if (msg.getWho().getMain() == who.getMain()) {
			// Component Command
			if (msg.getWhere() != null && msg.getWhere().equals(where)) {
				logger.debug("it is My (Component)");
				return true;

			// General Command
			//TODO: Da rivedere per il power manager
			} else if (msg.getWhere() != null && msg.getWhere().getMain() == 0) {
				logger.debug("it is My (General)");
				return true;

			// Area Command
			}  else if (msg.getWhere() != null && (msg.getWhere().getArea() == getWhere().getArea()) && (msg.getWhere().getPL() == 0)) {
				logger.debug("it is My (Area)");
				return true;
			}
		} else if (msg.getWho().getMain() == 2 && msg.getWhere().getMain() == group) {
			// Group Command
			logger.debug("it is My (Group)");
			return true;
		}

		return false;
	}

	@Override
	public SCSMsg getStatus() {
		SCSMsg msg = null;
		if (property == null) {
			msg = new SCSMsg(who, where, what);
		} else {
			msg = new SCSMsg(who, where, what, property, value);
		}

		return msg;
	}

	static SCSMsg createStatusMsg(int who, String area, String lightPoint, String value) {
		SCSMsg msg = null;
		try {
			if (value.contains("*")) {
				msg = new SCSMsg("*" + who + "*" + area + lightPoint + "*0*" + value + "##"); // Power
			} else {
				msg = new SCSMsg("*" + who + "*" + value + "*" + area + lightPoint + "##"); // All Other
			}
		} catch (MessageFormatException e) {
			logger.error("Error:", e);
		}
		return msg;
	}

	public void sendMsgToBus(SCSMsg msg) {
		try {
			if (msgOut.size() > 0)
				msgOut.take();

			msgOut.put(msg);
			logger.debug("Add to queue: " + msg.toString());
		} catch (InterruptedException e) {
			logger.error("Error:", e);
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void run() {
		logger.trace("Start run");
		SCSMsg msg = null;
		while (!Config.getInstance().isExit()) {
			try {
				if (msg == null) {
					msg = msgOut.take();
					logger.debug("Prepared msg to send.");
				} else {
					if (msgOut.size() > 0) {
						msg = msgOut.take();
						logger.debug("take a new msg to send.");
					}

				}

				if (bus.isReady()) {
					logger.debug("msg send ... {}", msg.toString());
					bus.sendCommand(msg, this);
					msg = null;
				}
			} catch (InterruptedException e) {
				logger.error("Error:", e);

				// if was call an interrupt then the thread need to die
				Thread.currentThread().interrupt();
			}
		}
		logger.trace("End run");
	}
}
