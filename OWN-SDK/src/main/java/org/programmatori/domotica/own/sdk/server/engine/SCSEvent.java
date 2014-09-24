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
package org.programmatori.domotica.own.sdk.server.engine;

import java.util.EventObject;

import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;

/**
 * SCS Event
 * 
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0.0, 21/03/2010
 */
public class SCSEvent extends EventObject {
	private static final long serialVersionUID = -7415969899219144543L;
	//private static final Log log = LogFactory.getLog(EventObject.class);

	private SCSMsg msg = null;

	public SCSEvent(Object source) {
		super(source);
	}

	public SCSEvent(Object source, SCSMsg msg) {
		super(source);
		this.msg = msg;
	}

	public SCSEvent(Object source, String msg) {
		super(source);
		try {
			this.msg = new SCSMsg(msg);
		} catch (MessageFormatException e) {
			//log.error(LogUtility.getErrorTrace(e));
			e.printStackTrace();
		}
	}

	public SCSMsg getMessage() {
		return msg;
	}
}
