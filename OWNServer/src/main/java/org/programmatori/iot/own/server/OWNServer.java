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
package org.programmatori.iot.own.server;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.ServerMsg;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.programmatori.domotica.own.sdk.server.engine.Monitor;
import org.programmatori.domotica.own.sdk.server.engine.PlugIn;
import org.programmatori.domotica.own.sdk.server.engine.Sender;
import org.programmatori.domotica.own.sdk.server.engine.core.BusDriver;
import org.programmatori.domotica.own.sdk.utils.ReflectionUtility;
import org.programmatori.iot.own.server.network.TcpIpServer;
import org.programmatori.iot.own.server.engine.Command;
import org.programmatori.iot.own.server.engine.ListenerPriorityBlockingQueue;
import org.programmatori.iot.own.server.engine.MsgReceiver;
import org.programmatori.iot.own.server.engine.MsgSender;
import org.programmatori.iot.own.server.engine.QueueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Send and Receive message from clients and talk with the bus.
 * <p>
 * OWNServer load the Driver Engine that manage the bus and manage the queue
 * of receive and transmit message to the bus from client.<br>
 * <br>
 * This class is configured using a configuration file.<br>
 * In the configuration you need to use tag 'bus' and inside it a class
 * full qualified name of the Engine.<br>
 * <code>
 * &lt;bus&gt;Emulator&lt;/bus&gt;<br>
 * </code><br>
 * The Emulator Plugin is our default and no need to change if don't want other.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 29/06/2011
 */
public final class OWNServer implements Runnable, QueueListener, EngineManager {
	private static final long serialVersionUID = -1460745010256569626L;
	private static final Logger logger = LoggerFactory.getLogger(OWNServer.class);

	/**
	 * List of Msg waiting to be send
	 */
	private final ListenerPriorityBlockingQueue<Command> msgSending = new ListenerPriorityBlockingQueue<>();

	/**
	 * Bus driver
	 */
	private BusDriver busDriver;

	/**
	 * It's use for send msg from clients to bus
	 */
	private final MsgSender sender;

	/**
	 * It's use for receive msg from bus to clients
	 */
	private final MsgReceiver receiver;

	/**
	 * Time to wait before the bus say no replay to msg
	 */
	private final int sendTimeout;

	/**
	 * ???
	 */
	private boolean changeQueue;

	public OWNServer(File  configFile) {
		logger.trace("Start Create Instance");
		Thread.currentThread().setName("OWN Server");

		if (configFile != null) {
			Config.getInstance().setConfig(configFile);
		}

		String line1 = Config.SERVER_NAME + " is Copyright (C) 2010-2020 Moreno Cattaneo";
		String line2 = "This program comes with ABSOLUTELY NO WARRANTY.";
		String line3 = "This is free software, and you are welcome to redistribute it";
		String line4 = "under certain conditions.";
		String line5 = "----";

		logger.info(line1);
		logger.info(line2);
		logger.info(line3);
		logger.info(line4);
		logger.info(line5);
		logger.info("{} v.{} Start", Config.SERVER_NAME, Config.SERVER_VERSION);

		//Load Bus Driver
		loadDriver();

		msgSending.addListener(this);
		changeQueue = false;

		sender = new MsgSender(busDriver, msgSending);
		sender.start();

		receiver = new MsgReceiver(busDriver, msgSending);
		msgSending.addListener(receiver);
		receiver.start();

		sendTimeout = Config.getInstance().getSendTimeout();

		// load Module
		loadPlugIn();

		TcpIpServer tcpIpServer = new TcpIpServer(this);
		tcpIpServer.start();

		logger.trace("End Create Instance");
	}

	/**
	 * Load and start the Bus
	 */
	private void loadDriver() {
		try {
			String busName = Config.getInstance().getBus();
			logger.debug("Engine Class Name: {}", busName);

			busDriver = ReflectionUtility.createClass(busName);
			if (busDriver != null) {
				logger.info("Engine starting: {}", busDriver.getName());

				busDriver.start();
			}

		} catch (NoClassDefFoundError e) {
			// TODO: Fix with new serial
			if (e.getMessage().contains("SerialPortEventListener")) {
				logger.error("You must install RXTX library (http://rxtx.qbang.org/)");
			} else {
				throw e;
			}
			System.exit(-1);

		} catch (IOException e) {
			logger.error("Error to start engine", e);
			System.exit(-3);

		} catch (Exception e) {
			logger.error("Error", e);
			System.exit(-2);
		}
	}

	private void loadPlugIn() {
		List<String> plugins = Config.getInstance().getPlugIn();
		for (String nameClass : plugins) {
			logger.debug("Try to load plugins {}", nameClass);

			try {
				PlugIn plugIn = ReflectionUtility.createClass(nameClass, (EngineManager) this);

				if (plugIn != null) {
					plugIn.start();
					logger.info("{} started!", plugIn.getClass().getSimpleName());

				} else {
					logger.error("Plugin {} not exist", nameClass);
				}

			} catch (Exception e) {
				logger.error("Error:", e);
			}
		}
	}

	@Override
	public void run() {
		logger.trace("Start run");
		while (!Config.getInstance().isExit()) {
			changeQueue = false;

			// Iterate sent command to see if i can replay to someone
			Iterator<Command> iter = msgSending.iterator();
			while (iter.hasNext() && !changeQueue) {
				//Command commandSended; = (Command) iter.next();
				Command commandSended = msgSending.peek();

				// searching a command that have status (with problem)
				if (commandSended != null) {
					if (commandSended.getStatus() != null) {
						boolean remove =msgSending.remove(commandSended);
						commandSended.getClient().receiveMsg(commandSended.getStatus());
						logger.debug("Reply to client by status: {} and remove: {}", commandSended.getStatus(), remove);

					// searching a msg with replay added
					} else if (!commandSended.getReceiveMsg().isEmpty()) {
						if (!isTimeWait(commandSended)) {
							boolean remove = msgSending.remove(commandSended);
							if (commandSended.getSendMsg().isStatus()) {
								logger.debug("request is status");
								logger.debug("Command: {}", commandSended);

								for (SCSMsg msg : commandSended.getReceiveMsg()) {
									logger.debug("msg to send to client: {}", msg);
									commandSended.getClient().receiveMsg(msg);
								}

							} else {
								logger.debug("request is command");
							}
							commandSended.getClient().receiveMsg(ServerMsg.MSG_ACK.getMsg());
							logger.debug("Reply to client by without status: ACK");
						}

					// searching a timeout msg
					} else {
						if (!Config.getInstance().isDebug()) {
							long now = Calendar.getInstance().getTimeInMillis();
							long create = commandSended.getTimeSend().getTime();
							if ((now - create) > sendTimeout) {
								msgSending.remove(commandSended);
								commandSended.getClient().receiveMsg(ServerMsg.MSG_NACK.getMsg());
								logger.debug("Reply to client by over time: NACK");
							}
						}
					}
				}
			}
		}

		if (sender != null && sender.isAlive()) sender.interrupt();
		if (receiver != null && receiver.isAlive()) receiver.interrupt();
		logger.trace("End run");
	}

	/**
	 * Indicate if can be wait for an answer. Use only for area message
	 */
	private boolean isTimeWait(Command command) {
		if (command.getSendMsg().isAreaMsg()) {
			long now = Calendar.getInstance().getTimeInMillis();

			long sent = command.getTimeSend().getTime();
			return (now - sent) < (sendTimeout - 1000);
		}

		return false;
	}

	public void sendCommand(SCSMsg msg, Sender client) {
		logger.trace("Start sendCommand");
		try {
			if (msg == null) throw new Exception("msg can't be empty");

			logger.debug("Msg Rx: {}", msg);
			Command command = new Command(client, msg);
			sender.sendToBus(command);
			receiver.sendToMonitor(msg); // TODO: it not arrive from bus ???

		} catch (Exception e) {
			logger.error("Error:", e);
		}

		logger.trace("End sendCommand");
	}

	public void addMonitor(Monitor monitor) {
		logger.trace("Start addMonitor");
		logger.debug("Add Monitor: {}", monitor.getId());
		receiver.addMonitor(monitor);
		logger.trace("End addMonitor");
	}

	public void removeMonitor(Monitor monitor) {
		logger.trace("Start removeMonitor");
		logger.debug("Remove Monitor: {}", monitor.getId());
		receiver.removeMonitor(monitor);
		logger.trace("End removeMonitor");
	}

	@Override
	public void changeNotify() {
		logger.trace("Start changeNotify");
		changeQueue = true;
		logger.trace("End changeNotify");
	}

	/**
	 * For start the Server
	 */
	public static void main(String[] args) {
		// Load config
		File configFile = null;
		if (args.length > 0) {
			configFile = new File(args[0]);
		}

		OWNServer server = new OWNServer(configFile);
		server.run();
	}
}
