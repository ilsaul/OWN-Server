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
package org.programmatori.domotica.own.engine.emulator.component;

import org.programmatori.domotica.own.sdk.component.Bus;
import org.programmatori.domotica.own.sdk.component.SCSComponent;
import org.programmatori.domotica.own.sdk.component.Who;
import org.programmatori.domotica.own.sdk.msg.RuntimeWrongMessageException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.ServerMsg;
import org.programmatori.domotica.own.sdk.msg.What;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Light, lamp.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 13/01/2015
 */
public class Light extends SCSBaseComponent {
	private static final long serialVersionUID = 6385568654373192697L;

	private static final Logger logger = LoggerFactory.getLogger(Light.class);

	public Light(SCSMsg msg, Bus bus) {
		super(msg, bus);

		if (msg.isStatus() || msg.getWho().getMain() != Who.LIGHT.getValue())
			throw new RuntimeWrongMessageException("Wrong Message in the creation of " + this.getClass().getSimpleName());

		if (getWhat() == null) setWhat(new What("0"));
	}

	@Override
	public void receiveMessage(SCSMsg msg) {
		logger.debug("MSG arrive to component Light {}: {}", getWhere(), msg);
		if (isMyMsg(msg)) {
			if (msg.isStatus()) {
				sendMsgToBus(getStatus());

			} else if (msg.getWhat() != null) {
				setWhat(msg.getWhat());
				sendMsgToBus(getStatus());

			} else {
				sendMsgToBus(ServerMsg.MSG_NACK.getMsg());
			}
		} else {
			logger.trace("The message {} is not for me ({})", msg, this);
		}
	}

	public static SCSComponent create(Bus bus, String area, String lightPoint, String value) {
		SCSMsg msg = createStatusMsg(Who.LIGHT.getValue(), area, lightPoint, value);
		logger.debug("Create Light Status: {}", msg);
		return new Light(msg, bus);
	}


}
