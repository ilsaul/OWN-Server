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
package org.programmatori.domotica.own.sdk.server.engine;

import org.programmatori.domotica.own.sdk.msg.SCSMsg;

import java.io.Serializable;
import java.lang.Thread.State;

/**
 * This interface expose to connected client the method for send to and
 * receive information from the bus.
 *
 * old:
 * This interface is for connect the class that need to talk with the bus with it.
 * Is the interface for plugin and engine of the EngineManagerImpl that is in Server
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 22/02/2013
 */
public interface EngineManager extends Serializable {

	/**
	 * Use for send command to the bus
	 * @param msg SCSMsg that you want to send
	 * @param client Who send the message
	 */
	void sendCommand(SCSMsg msg, Sender client);

	/**
	 * Add a client to the list of those receiving messages that pass for the bus
	 */
	void addMonitor(Monitor monitor);

	/**
	 * remove a client from the list of those receiving messages that pass for the bus
	 */
	void removeMonitor(Monitor monitor);

	/**
	 * Start Engine
	 */
	void start();

	/**
	 * Status of the engine
	 */
	State getState();
}
