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

import java.io.IOException;
import java.util.TooManyListenersException;

import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.SCSListener;

/**
 * Base Interface if someone want to implemented a new engine.
 * The Engine is a component that TcpIpServer use to talk with SCS BUS.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since TcpIpServer 1.0.0
 */
public interface Engine {

	/**
	 * If the engine can start to use.
	 * @return true if is ready
	 */
	boolean isReady();

	/**
	 * Use from server to send a command to the bus
	 * @param msg message that it want to send
	 */
	void sendCommand(SCSMsg msg) throws IOException;

	/**
	 * The client connection add itself to a list of listener of the bus for receive a msg from BUS
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
}
