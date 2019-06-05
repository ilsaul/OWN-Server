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
package org.programmatori.domotica.own.engine.emulator.component;

import org.programmatori.domotica.own.sdk.msg.RuntimeWrongMessageException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.What;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Blind, shade, jalousie.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.1.1, 22/02/2013
 */
public class Blind extends SCSBaseComponent {
	private static final long serialVersionUID = 5225952158561315171L;

	private static final Logger logger = LoggerFactory.getLogger(Blind.class);

	public static final int MUST_WHO = 2;
	public static final int WHAT_UP = 2;
	public static final int WHAT_DOWN = 1;
	public static final int WHAT_STOP = 0;
	public static final String NAME = "blind";

	public Blind(SCSMsg msg, Bus bus) {
		super(msg, bus);

		if (msg.isStatus() || msg.getWho().getMain() != MUST_WHO)
			throw new RuntimeWrongMessageException("Wrong Message in the creation of " + this.getClass().getSimpleName());

		if (getWhat() == null)
			setWhat(new What("0"));
	}

	@Override
	public void receiveMessage(SCSMsg msg) {
		if (isMyMsg(msg)) {
			if (msg.isStatus()) {
				sendMsgToBus(getStatus());
			} else if (msg.getWhat() != null) {
				setWhat(msg.getWhat());
				sendMsgToBus(getStatus());
			} else {
				sendMsgToBus(SCSMsg.MSG_NACK);
			}
		}
	}

	public static SCSComponent create(Bus bus, String area, String lightPoint, String value) {
		SCSMsg msg = createStatusMsg(MUST_WHO, area, lightPoint, value);
		logger.debug("Create Blind Status: {}", msg);
		return new Blind(msg, bus);
	}
}
