/*
 * OWN Server is 
 * Copyright (C) 2010-2012 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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
package org.programmatori.domotica.own.server.engine;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.*;
import org.programmatori.domotica.own.sdk.server.engine.core.Engine;
import org.programmatori.domotica.own.sdk.utils.LogUtility;

/**
 * This class manage the message came from the bus
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0.0, 16/10/2010
 * @since OWNServer v0.1.0
 */
public class MsgReceiver extends Thread implements SCSListener, QueueListener {
	private static final Log log = LogFactory.getLog(MsgReceiver.class);

	private BlockingQueue<Command> msgReceiveFromBus;
	private BlockingQueue<Command> msgSended;
	private Engine engine;
	private List<Monitor> monitors;
	private boolean changeQueue;

	public MsgReceiver(Engine engine, BlockingQueue<Command> queueSended) {
		log.trace("Start Create Istance");
		setName("MsgReceiver");
		setDaemon(true);
		Config.getInstance().addThread(this);

		msgReceiveFromBus = new LinkedBlockingQueue<Command>();
		this.engine = engine;
		msgSended = queueSended;
		changeQueue = false;
		monitors = new ArrayList<Monitor>();
		try {
			this.engine.addEventListener(this);
		} catch (TooManyListenersException e) {
			log.error(LogUtility.getErrorTrace(e));
		}
		log.trace("End Create Istance");
	}

	@Override
	public void run() {
		log.trace("Start Run");
		while (!Config.getInstance().isExit()) {
			Command command = null;
			try {
				command = msgReceiveFromBus.take();
			} catch (InterruptedException e) {
				log.error(LogUtility.getErrorTrace(e));
			}

			if (command != null) {
				SCSMsg received = null;
				if (command.getReceiveMsg().size() > 0) received = command.getReceiveMsg().get(0); // From msgReceiveFromBus is only 1 msg
				log.debug("msg 0: " + received);

				if (received != null) {
					sendToMonitor(received);
					log.debug("RX from Bus " + command.toString());

					while (received != null) {
						changeQueue = false;

						//FIXME: Iterator don't follow the order of the priority
						log.debug("Queue sended: " + msgSended.size());
						Iterator<Command> iter = msgSended.iterator();
						while (iter.hasNext() && !changeQueue && received != null) {
							Command commandSended = (Command) iter.next();
							log.debug("Command to examinate: " + commandSended.toString());

							// Don't elaborate again the Binded Msg
							//if (commandSended.getTimeAnswer() == null) {
							SCSMsg sended = commandSended.getSendMsg();
							log.debug("Msg to examinate: " + sended.toString());

							// Search if it is the same msg
							//TODO: Improve for other case
							if (sended.getWho().equals(received.getWho())) {

								// Same Command
								if (sended.getWhere().equals(received.getWhere())) {
									if (sended.isStatus() || sended.getWhat().equals(received.getWhat())) {
										addResponse(command, commandSended);
										received = null;
									}

								// Area Command
								} else if (sended.isAreaMsg() && sended.getWhere().getArea() == received.getWhere().getArea()) {
									addResponse(command, commandSended);
									received = null;

								// Bus Command (same Who)
								} else if (sended.isWhoMsg()) {
									addResponse(command, commandSended);
									received = null;
								}
							}
						}
						if (!iter.hasNext()) received = null;
					}
				}
			}
		}

		log.trace("End Run");
	}

	private void addResponse(Command commandReceived, Command commandSended) {
		msgSended.remove(commandSended);
		commandSended.setReceived(commandReceived);
		log.debug("Msg Bind create: " + commandSended.toString());
		try {
			msgSended.put(commandSended);
		} catch (InterruptedException e) {
			log.error(LogUtility.getErrorTrace(e));
		}
	}

	@Override
	public void SCSValueChanged(SCSEvent e) {
		log.trace("Start SCSValueChanged");
		SCSMsg msg = e.getMessage();

		Command command = new Command(null, msg);
		try {
			msgReceiveFromBus.put(command);
			log.debug("Recived queue: " + msgReceiveFromBus.size());
		} catch (InterruptedException e1) {
			log.error(LogUtility.getErrorTrace(e1));
		}

		log.trace("End SCSValueChanged");
	}

	public void addMonitor(Monitor monitor) {
		log.trace("Start addMonitor");
		log.debug("Add Monitor: " + monitor.getId());
		monitors.add(monitor);
		log.trace("End addMonitor");
	}

	public void removeMonitor(Monitor monitor) {
		log.trace("Start removeMonitor");
		log.debug("Remove Monitor: " + monitor.getId());
		monitors.remove(monitor);
		log.trace("End removeMonitor");
	}

	public void sendToMonitor(SCSMsg msg) {
		log.trace("Start sendToMonitor");
		for (Iterator<Monitor> iter = monitors.iterator(); iter.hasNext();) {
			Monitor monitor = (Monitor) iter.next();

			monitor.reciveMsg(msg);
		}
		log.trace("End sendToMonitor");
	}

	@Override
	public void changeNotify() {
		log.trace("Start changeNotify");
		changeQueue = true;
		log.trace("End changeNotify");
	}
}
