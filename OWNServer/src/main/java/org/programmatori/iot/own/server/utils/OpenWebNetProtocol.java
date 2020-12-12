/*
 * Copyright (C) 2010-2020 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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
package org.programmatori.domotica.own.server.utils;

import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.ServerMsg;
import org.programmatori.iot.own.server.network.ClientSessionType;

/**
 * This class maintain some constant for the protocol SCS
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 16/10/2010
 */
public class OpenWebNetProtocol {
	public static final SCSMsg MSG_WELCOME = ServerMsg.MSG_ACK.getMsg();

	public static final String STATUS_CHAR_WHO = "#";

	/** @deprecated use {@link ClientSessionType} */
	@Deprecated
	public static final int MODE_COMMAND = 0;
	//public static final int MODE_MONITOR = 1;
	//spublic static final int MODE_TEST = 2;

	private OpenWebNetProtocol() {
		// For not instantiate the class
	}
}
