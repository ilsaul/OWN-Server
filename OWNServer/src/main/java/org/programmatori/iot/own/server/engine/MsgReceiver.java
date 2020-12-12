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
package org.programmatori.domotica.own.server.engine;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.Monitor;
import org.programmatori.domotica.own.sdk.server.engine.SCSEvent;
import org.programmatori.domotica.own.sdk.server.engine.SCSListener;
import org.programmatori.domotica.own.sdk.server.engine.core.BusDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class manage the message came from the bus
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0.0, 16/10/2010
 * @since OWNServer v0.1.0
 */
public class MsgReceiver extends Thread implements SCSListener, QueueListener {
	private static final long serialVersionUID = 4398470202679549451L;
	private static final Logger logger = LoggerFactory.getLogger(MsgReceiver.class);

	private final BlockingQueue<Command> msgReceiveFromBus;
	private final BlockingQueue<Command> msgSended;
	private BusDriver busDriver;
	private final List<Monitor> monitors;
	private boolean changeQueue;

	public MsgReceiver(BusDriver busDriver, BlockingQueue<Command> queueSended) {
		logger.trace("Start Create Instance");
		setName("MsgReceiver");
		setDaemon(true);
		Config.getInstance().addThread(this);

		msgReceiveFromBus = new LinkedBlockingQueue<>();
		this.busDriver = busDriver;
		msgSended = queueSended;
		changeQueue = false;
		monitors = new ArrayList<>();
		try {
			this.busDriver.addEventListener(this);
		} catch (TooManyListenersException e) {
			logger.error("Error:" , e);
		}
		logger.trace("End Create Instance");
	}

	@Override
	public void run() {
		logger.trace("Start Run");
		while (!Config.getInstance().isExit()) {
			Command command = null;
			try {
				command = msgReceiveFromBus.take();

			} catch (InterruptedException e) {
				logger.error("Error:" , e);
			}

			if (command != null) {
				SCSMsg received = null;
				if (!command.getReceiveMsg().isEmpty()) received = command.getReceiveMsg().get(0); // From msgReceiveFromBus is only 1 msg
				logger.debug("msg 0: {}", received);

				if (received != null) {
					sendToMonitor(received);
					logger.debug("RX from Bus {}", command);

					while (received != null) {
						changeQueue = false;

						//FIXME: Iterator don't follow the order of the priority
						logger.debug("Queue sending: {}", msgSended.size());
						Iterator<Command> iter = msgSended.iterator();
						while (iter.hasNext() && !changeQueue && received != null) {
							Command commandSended = iter.next();
							logger.debug("Command to examine: {}", commandSended);

							SCSMsg sent = commandSended.getSendMsg();
							logger.debug("Msg to examine: {}", sent);

							// Search if it is the same msg
							//TODO: Improve for other case
							if (sent.getWho().getMain() == received.getWho().getMain()) {
								logger.trace("Who match {} between TX: {} & RX: {}", sent.getWho(), sent, received);

								// Same Command
								if (sent.getWhere().equals(received.getWhere())) {
									logger.trace("Where match {} between TX: {} & RX: {}", sent.getWhere(), sent, received);

									if (sent.isStatus() || sent.getWhat().equals(received.getWhat())) {
										logger.debug("Sender {} request status and receive {}", sent, received);
										addResponse(command, commandSended);
										received = null;
									} else {
										logger.warn("Unknown situation");
									}

								// Area Command
								} else if (sent.isAreaMsg() && sent.getWhere().getArea() == received.getWhere().getArea()) {
									logger.debug("Area match: Sender {} receive {}", sent, received);
									addResponse(command, commandSended);
									received = null;

								// Bus Command (same Who)
								} else if (sent.isWhoMsg()) {
									logger.debug("TODO: Sender {} receive {}", sent, received);
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

		logger.trace("End Run");
	}

	private void addResponse(Command commandReceived, Command commandSended) {
		msgSended.remove(commandSended);
		commandSended.setReceived(commandReceived);
		logger.debug("Msg Bind create: {}", commandSended);
		try {
			msgSended.put(commandSended);

		} catch (InterruptedException e) {
			logger.error("Error:" , e);
		}
	}

	@Override
	public void scsValueChanged(SCSEvent e) {
		logger.trace("Start SCSValueChanged");
		SCSMsg msg = e.getMessage();

		Command command = new Command(null, msg);
		try {
			msgReceiveFromBus.put(command);
			logger.debug("Received queue: {}", msgReceiveFromBus.size());

		} catch (InterruptedException e1) {
			logger.error("Error:" , e);
		}

		logger.trace("End SCSValueChanged");
	}

	public void addMonitor(Monitor monitor) {
		logger.trace("Start addMonitor");
		logger.debug("Add Monitor: {}", monitor.getId());
		monitors.add(monitor);
		logger.trace("End addMonitor");
	}

	public void removeMonitor(Monitor monitor) {
		logger.trace("Start removeMonitor");
		logger.debug("Remove Monitor: {}", monitor.getId());
		monitors.remove(monitor);
		logger.trace("End removeMonitor");
	}

	public void sendToMonitor(SCSMsg msg) {
		logger.trace("Start sendToMonitor");
		for (Iterator<Monitor> iter = monitors.iterator(); iter.hasNext();) {
			Monitor monitor = (Monitor) iter.next();

			monitor.receiveMsg(msg);
		}
		logger.trace("End sendToMonitor");
	}

	@Override
	public void changeNotify() {
		logger.trace("Start changeNotify");
		changeQueue = true;
		logger.trace("End changeNotify");
	}
}
