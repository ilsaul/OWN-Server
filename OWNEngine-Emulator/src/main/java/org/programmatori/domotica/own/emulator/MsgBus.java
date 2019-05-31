/*
 * Copyright (C) 2010-2016 Moreno Cattaneo <moreno.cattaneo@gmail.com>
 *
 * This file is part of OWn Server.
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
package org.programmatori.domotica.own.emulator;

import java.io.Serializable;

import org.programmatori.domotica.own.sdk.msg.SCSMsg;

/**
 * Internal use of SCS Bus
 * @author Moreno
 */
public class MsgBus implements Serializable {
	private static final long serialVersionUID = -8067209723766915723L;

	private SCSMsg msg;
	private SCSComponent component;

	public MsgBus(SCSMsg msg, SCSComponent component) {
		this.msg = msg;
		this.component = component;
	}

	public SCSMsg getMsg() {
		return msg;
	}
	public void setMsg(SCSMsg msg) {
		this.msg = msg;
	}
	public SCSComponent getComponent() {
		return component;
	}
	public void setComponent(SCSComponent component) {
		this.component = component;
	}


}
