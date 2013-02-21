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
package org.programmatori.domotica.own.emulator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.What;

public class Blind extends SCSBaseComponent {
	private static final Log log = LogFactory.getLog(Blind.class);

	public static final int MUST_WHO = 2;
	public static final int WHAT_UP = 2;
	public static final int WHAT_DOWN = 1;
	public static final int WHAT_STOP = 0;

	public Blind(SCSMsg msg, Bus bus) {
		super(msg, bus);

		if (msg.isStatus() || msg.getWho().getMain() != MUST_WHO)
			throw new RuntimeException("Wrong Message for create " + this.getClass().getSimpleName());

		if (getWhat() == null) setWhat(new What("0"));
	}

	@Override
	public void reciveMessage(SCSMsg msg) {
		if (isMyMsg(msg)) {
			if (msg.isStatus()) {
				sendMsgToBus(getStatus());
				//bus.sendCommand(SCSMsg.MSG_ACK);
			} else if (msg.getWhat() != null) { // && !getWhat().equals(msg.getWhat())) {
				setWhat(msg.getWhat());
				sendMsgToBus(getStatus());
				//bus.sendCommand(SCSMsg.MSG_ACK);
			} else {
				sendMsgToBus(SCSMsg.MSG_NACK);
			}
		}
	}

	public static SCSComponent create(Bus bus, String area, String lightPoint, String value) {
		SCSMsg msg = createStatusMsg(MUST_WHO, area, lightPoint, value);
		log.debug("Create Blind Status: " + msg);
		return new Blind(msg, bus);
	}
}
