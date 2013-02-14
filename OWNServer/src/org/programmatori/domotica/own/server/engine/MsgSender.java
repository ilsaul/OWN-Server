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

import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.core.Engine;
import org.programmatori.domotica.own.sdk.utils.LogUtility;

public class MsgSender extends Thread {
	private static final Log log = LogFactory.getLog(MsgSender.class);

	private BlockingQueue<Command> msgSendToBus;
	private BlockingQueue<Command> msgSended;
	private Engine engine;
	private int sendTimeout;

	public MsgSender(Engine engine, BlockingQueue<Command> queueSended) {
		log.trace("Start Create Istance");
		setName("MsgSender");
		setDaemon(true);
		Config.getInstance().addThread(this);

		msgSendToBus = new LinkedBlockingQueue<Command>();
		msgSended = queueSended;
		this.engine = engine;

		sendTimeout = Config.getInstance().getSendTimeout();
		log.trace("End Create Istance");
	}

	@Override
	public void run() {
		log.trace("Start Run");
		while (!Config.getInstance().isExit()) {
			Command command = null;
			try {
				if (command == null) {
					command = msgSendToBus.take();
					log.debug("Received Command: " + command.toString());
				}

				if (engine.isReady()) {
					engine.sendCommand(command.getSendMsg());
					command.setTimeSend(Calendar.getInstance().getTime());
					msgSended.put(command);
					log.debug("TX To Bus " + command.toString());

					command = null;
				} else {
					// Bus is not ready to receive msg
					long now = Calendar.getInstance().getTimeInMillis();
					long create = command.getTimeCreate().getTime();
					if ((now - create) > sendTimeout) {
						command.setStatus(SCSMsg.MSG_COLL);
						command.setTimeSend(Calendar.getInstance().getTime());
						msgSended.put(command);
						log.debug("TX To Bus " + command.toString());
						command = null;

					}
				}
			} catch (InterruptedException e) {
				log.error(LogUtility.getErrorTrace(e));
			}
		}

		log.trace("End Run");
	}

	public void sendToBus(Command command) throws InterruptedException {
		log.trace("Start sendToBus");
		msgSendToBus.put(command);
		log.debug("Sending queue: " + msgSendToBus.size());
		log.trace("End sendToBus");
	}
}
