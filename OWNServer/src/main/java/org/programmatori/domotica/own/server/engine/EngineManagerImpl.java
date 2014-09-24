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

import java.lang.reflect.Constructor;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.*;
import org.programmatori.domotica.own.sdk.server.engine.core.Engine;
import org.programmatori.domotica.own.sdk.utils.LogUtility;

/**
 * EngineManager Load the class that manage the bus. This class and is 
 * parameter need to be set in the config file.<br>
 * In configuration need to use tag 'bus' with the full-name class.<br>
 * <code>
 * &lt;bus&gt;org.programmatori.domotica.own.server.engine.core.Emulator&lt;/bus&gt;<br>
 * </code><br>
 * For use with {@link Emulator} no need to change this part of 
 * configuration.
 * 
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0.1, 29/06/2011
 * @since OWNServer v0.2.0
 *
 */
public class EngineManagerImpl extends Thread implements QueueListener, EngineManager {
	private static final Log log = LogFactory.getLog(EngineManagerImpl.class);

	/**
	 * List of Msg waiting to be send
	 */
	private ListenerPriorityBlockingQueue<Command> msgSended;
	
	/**
	 * Bus manager
	 */
	private Engine engine;
	
	/**
	 * Thread for send msg
	 */
	private MsgSender sender;
	
	/**
	 * Thread for receive msg
	 */
	private MsgReceiver receiver;
	
	/**
	 * Time to wait before the bus say no replay to msg
	 */
	private int sendTimeout;
	private boolean changeQueue;

	public EngineManagerImpl() {
		log.trace("Start Create Istance");
		setName("SCS Engine");
		//setDaemon(true);
		Config.getInstance().addThread(this);

		//Load Engine
		try {
			String busName = Config.getInstance().getBus();
			log.debug("Engine Class Name: " + busName);

			Class<?> c = ClassLoader.getSystemClassLoader().loadClass(busName);
			engine = (Engine) c.newInstance();
			//engine.addEventListener(this);
		} catch (NoClassDefFoundError e) {
			if (e.getMessage().indexOf("SerialPortEventListener") > -1) {
				log.error("You must install RXTX library (http://rxtx.qbang.org/)");
			} else {
				throw e;
			}
			System.exit(-1);
		} catch (Exception e) {
			log.error(LogUtility.getErrorTrace(e));
			System.exit(-1);
		}

		msgSended = new ListenerPriorityBlockingQueue<Command>();
		msgSended.addListener(this);
		changeQueue = false;

		//monitors = new ArrayList<Monitor>();
		sender = new MsgSender(engine, msgSended);
		sender.start();
		receiver = new MsgReceiver(engine, msgSended);
		msgSended.addListener(receiver);
		receiver.start();
		sendTimeout = Config.getInstance().getSendTimeout();

		// load Module
		loadPlugIn();

		log.trace("End Create Istance");
	}

	private void loadPlugIn() {
		//org.programmatori.domotica.bticino.map.Map map = new org.programmatori.domotica.bticino.map.Map(this);
		//map.start();

		List<String> plugins = Config.getInstance().getPlugIn();
		for (Iterator<String> iter = plugins.iterator(); iter.hasNext();) {
			String nameClass = (String) iter.next();

			try {
				Class<?> c = ClassLoader.getSystemClassLoader().loadClass(nameClass);
				@SuppressWarnings("unchecked")
				Constructor<PlugIn> constructor = (Constructor<PlugIn>) c.getConstructor(EngineManagerImpl.class);

				PlugIn plugIn = constructor.newInstance(this);
				plugIn.start();
				log.info(plugIn.getClass().getSimpleName() + " started!");
			} catch (Exception e) {
				log.error(LogUtility.getErrorTrace(e));
			}
		}
	}

	@Override
	public void run() {
		log.trace("Start run");
		while (!Config.getInstance().isExit()) {
			changeQueue  = false;

			// Iterate sent command to see if i can replay to someone
			Iterator<Command> iter = msgSended.iterator();
			while (iter.hasNext() && !changeQueue) {
				Command commandSended = (Command) iter.next();
				commandSended = msgSended.peek();

				// searching a command that have status (with problem)
				if (commandSended != null) {
					if (commandSended.getStatus() != null) {
						msgSended.remove(commandSended);
						commandSended.getClient().reciveMsg(commandSended.getStatus());
						log.debug("Reply to client by status: " + commandSended.getStatus().toString());

					// searching a msg with replay added
					} else if (commandSended.getReceiveMsg().size() > 0) {
						if (!isTimeWait(commandSended)) {
							msgSended.remove(commandSended);
							if (commandSended.getSendMsg().isStatus()) {
								log.debug("requst is status");
								log.debug("Command: " + commandSended);

								for (Iterator<SCSMsg> iter2 = commandSended.getReceiveMsg().iterator(); iter2.hasNext();) {
									SCSMsg msg = (SCSMsg) iter2.next();
									log.debug("msg to send to client: " + msg);
									commandSended.getClient().reciveMsg(msg);
								}

							} else {
								log.debug("requst is command");
							}
							commandSended.getClient().reciveMsg(SCSMsg.MSG_ACK);
							log.debug("Reply to client by without status: ACK");
						}

					// searching a timeout msg
					} else {
						if (!Config.getInstance().isDebug()) {
							long now = Calendar.getInstance().getTimeInMillis();
							long create = commandSended.getTimeSend().getTime();
							if ((now - create) > sendTimeout) {
								msgSended.remove(commandSended);
								commandSended.getClient().reciveMsg(SCSMsg.MSG_NACK);
								log.debug("Reply to client by over time: NACK");
							}
						}
					}
				}
			}
		}

		if (sender != null && sender.isAlive()) sender.interrupt();
		if (receiver != null && receiver.isAlive()) receiver.interrupt();
		log.trace("End run");
	}

	private boolean isTimeWait(Command command) {
		boolean ret = false;

		if (command.getSendMsg().isAreaMsg()) {
			long now = Calendar.getInstance().getTimeInMillis();

			long sended = command.getTimeSend().getTime();
			if ((now - sended) < (sendTimeout - 1000)) {
				ret = true;
			}
		}

		return ret;
	}

	public void sendCommand(SCSMsg msg, Sender client) {
		log.trace("Start sendCommand");
		try {
			if (msg == null) throw new Exception("msg can't be empty");

			log.debug("Msg Rx: " + msg);
			Command command = new Command(client, msg);
			sender.sendToBus(command);
			receiver.sendToMonitor(msg);

		} catch (Exception e) {
			log.error(LogUtility.getErrorTrace(e));
		}

		log.trace("End sendCommand");
	}

	public void addMonitor(Monitor monitor) {
		log.trace("Start addMonitor");
		log.debug("Add Monitor: " + monitor.getId());
		receiver.addMonitor(monitor);
		log.trace("End addMonitor");
	}

	public void removeMonitor(Monitor monitor) {
		log.trace("Start removeMonitor");
		log.debug("Remove Monitor: " + monitor.getId());
		receiver.removeMonitor(monitor);
		log.trace("End removeMonitor");
	}

	@Override
	public void changeNotify() {
		log.trace("Start changeNotify");
		changeQueue = true;
		log.trace("End changeNotify");
	}
}
