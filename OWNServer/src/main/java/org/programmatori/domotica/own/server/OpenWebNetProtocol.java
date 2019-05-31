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
package org.programmatori.domotica.own.server;

import org.programmatori.domotica.own.sdk.msg.SCSMsg;

/**
 * This class maintain some constant for the protocol SCS
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0.0, 16/10/2010
 * @since OWNServer v0.1.0
 */
public class OpenWebNetProtocol {
	public static final SCSMsg MSG_WELCOME = SCSMsg.MSG_ACK;
	public static SCSMsg MSG_MODE_COMMAND;
	public static SCSMsg MSG_MODE_MONITOR;
	public static SCSMsg MSG_MODE_TEST;
	public static final String STATUS_CHAR_WHO = "#";

	public static final int MODE_COMMAND = 0;
	public static final int MODE_MONITOR = 1;
	public static final int MODE_TEST = 2;

	static {
		try {
			MSG_MODE_COMMAND = new SCSMsg("*99*0##");
			MSG_MODE_MONITOR = new SCSMsg("*99*1##");
			MSG_MODE_TEST = new SCSMsg("*99*2##");
	    } catch ( Exception e ) {
	      // Stub !!
	    }
	}

	private OpenWebNetProtocol() {
		// per non instanziare la classe
	}
}
