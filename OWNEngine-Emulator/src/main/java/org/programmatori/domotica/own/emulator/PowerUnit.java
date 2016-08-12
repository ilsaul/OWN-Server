/*
 * Copyright (C) 2010-2015 Moreno Cattaneo <moreno.cattaneo@gmail.com>
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

import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the Central Power Unit
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0.1, 13/01/2015
 * @since OWNServer 0.4.0
 */
public class PowerUnit extends SCSBaseComponent {
	private static final long serialVersionUID = 6661218877728692531L;

	private static final Logger logger = LoggerFactory.getLogger(PowerUnit.class);

	public static final int MUST_WHO = 3;

	public PowerUnit(SCSMsg msg, Bus bus) {
		super(msg, bus);

		if (msg.isStatus() || msg.getWho().getMain() != MUST_WHO)
			throw new RuntimeException("Wrong Message for create " + this.getClass().getSimpleName());

		if (getWhat() != null) setWhat(null);
	}

	/**
	 * Accepted Command:<br>
	 * *#3*10*0##: Request All Information<br>
	 * *#3*10*1##: Request specific information (from 1 to 4)<br>
	 * <br>
	 * Specific Value:<br>
	 * 1: Volt<br>
	 * 2: Ampere<br>
	 * 3: Watt<br>
	 * 4: energy (I don't know)<br>
	 *
	 */
	@Override
	public void reciveMessage(SCSMsg msg) {
		logger.debug("MSG arrive to component: {}", msg.toString());
		if (isMyMsg(msg)) {
			if (msg.isStatus()) {

				// All priority
				if (msg.getProperty().getMain() == 0) {
					sendMsgToBus(getStatus());

				// Single Priority
				} else {
					int pos = msg.getProperty().getMain();

					String value = null;
					if (pos == 1) value = "" + getValue().getMain();
					else value = getValue().getSingleValue(pos-2);

					// Replay: *#3*10*3*P##
					Value msgValue = new Value(value);
					SCSMsg replayMsg = new SCSMsg(msg.getWho(), msg.getWhere(), msg.getWhat(), msg.getProperty(), msgValue);

					sendMsgToBus(replayMsg);
				}
				//bus.sendCommand(SCSMsg.MSG_ACK);
//			} else if (msg.getWhat() != null) { // && !getWhat().equals(msg.getWhat())) {
//				setWhat(msg.getWhat());
//				sendMsgToBus(getStatus());
				//bus.sendCommand(SCSMsg.MSG_ACK);
			} else {
				sendMsgToBus(SCSMsg.MSG_NACK);
			}
		}
	}

	public static SCSComponent create(Bus bus, String area, String t, String c, String p, String e) {
		SCSMsg msg = createStatusMsg(MUST_WHO, area, "", t + "*" + c + "*" + p + "*" + e);
		logger.debug("Create Light Status: {}", msg);
		return new PowerUnit(msg, bus);
	}

	public static SCSComponent create(Bus bus, String area, String value) {
		SCSMsg msg = createStatusMsg(MUST_WHO, area, "", value);
		logger.debug("Create Power Unit Status: {}", msg);
		return new PowerUnit(msg, bus);
	}
}
