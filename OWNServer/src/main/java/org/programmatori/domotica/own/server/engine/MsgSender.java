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

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.programmatori.domotica.own.sdk.config.Config;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.ServerMsg;
import org.programmatori.domotica.own.sdk.server.engine.core.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class send msg to the bus.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0.1, 27/02/2013
 * @since OWNServer v0.1.0
 */
public class MsgSender extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(MsgSender.class);

	private final BlockingQueue<Command> msgSendToBus;
	private final BlockingQueue<Command> msgSended;
	private final Engine engine;
	private final int sendTimeout;

	public MsgSender(Engine engine, BlockingQueue<Command> queueSended) {
		logger.trace("Start Create Instance");
		setName("MsgSender");
		setDaemon(true);
		Config.getInstance().addThread(this);

		msgSendToBus = new LinkedBlockingQueue<>();
		msgSended = queueSended;
		this.engine = engine;

		sendTimeout = Config.getInstance().getSendTimeout();
		logger.trace("End Create Instance");
	}

	@Override
	public void run() {
		logger.trace("Start Run");
		while (!Config.getInstance().isExit()) {
			Command command = null;
			try {
				if (command == null) {
					command = msgSendToBus.take();
					logger.debug("Received Command: {}", command.toString());
				}

				if (engine.isReady()) {
					try {
						engine.sendCommand(command.getSendMsg());
						command.setTimeSend(Calendar.getInstance().getTime());
						msgSended.put(command);
						logger.debug("TX To Bus {}", command.toString());
					} catch (IOException e) {
						logger.error("Error in send command", e);
					}

					command = null;
				} else {
					// Bus is not ready to receive msg
					long now = Calendar.getInstance().getTimeInMillis();
					long create = command.getTimeCreate().getTime();
					if ((now - create) > sendTimeout) {
						command.setStatus(ServerMsg.MSG_COLL.getMsg());
						command.setTimeSend(Calendar.getInstance().getTime());
						msgSended.put(command);
						logger.debug("TX To Bus {}", command.toString());
						command = null;

					}
				}
			} catch (InterruptedException e) {
				logger.error("Error:" , e);
			}
		}

		logger.trace("End Run");
	}

	public void sendToBus(Command command) throws InterruptedException {
		logger.trace("Start sendToBus");
		msgSendToBus.put(command);
		logger.debug("Sending queue: {}", msgSendToBus.size());
		logger.trace("End sendToBus");
	}
}
