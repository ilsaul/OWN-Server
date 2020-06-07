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
package org.programmatori.domotica.own.sdk.server.engine.core;

import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.SCSListener;

import java.io.IOException;
import java.util.TooManyListenersException;

/**
 * This is the driver that knows how to talk with the bus. Manage any
 * message between EngineManager and Bus.
 * This is the Base Interface if someone want to implement a new engine.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 */
public interface Engine {

	/**
	 * If the engine is ready for receive and send message.
	 * @return true if is ready
	 */
	boolean isReady();

	/**
	 * Use from client to send a command to the bus
	 * @param msg message that it want to send
	 */
	void sendCommand(SCSMsg msg) throws IOException;

	/**
	 * The client add itself to a list of observer of the bus for receive any msg from BUS
	 * @param listener what want receive the message from the bus
	 * @throws TooManyListenersException Exception if too musch client is connetced
	 */
	void addEventListener(SCSListener listener) throws TooManyListenersException;

	/**
	 * Remove from list of listener
	 * @param listener The object that Previously ask to receive msg from bus
	 */
	void removeEventListener(SCSListener listener);

	/**
	 * Start to work, normally mean connect to the device.
	 */
	void start() throws IOException;

	/**
	 * It is use for close connection with the bus
	 */
	void close();

	/**
	 * Name of the Engine
	 */
	String getName();
}
