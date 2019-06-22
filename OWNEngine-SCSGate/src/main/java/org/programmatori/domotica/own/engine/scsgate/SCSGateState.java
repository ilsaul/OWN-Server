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
package org.programmatori.domotica.own.engine.scsgate;

public enum SCSGateState {

	STATE_INITIAL("Connecting Arduino ..."),
	STATE_ARDUINO_READY("Arduino Ready"),
	//STATE_SCS_GATE_SET_SLOW_SPEED("Speed SCSGate to 38400 baud"),
	STATE_SCS_GATE_SET_VOLT("Volt SCSGate to +5V"),
	STATE_SCS_GATE_SET_MODE("Binary Mode in SCSGate"),
	STATE_SCS_GATE_SET_LOG("Log Mode in SCSGate to continuative Log"),
	STATE_SCS_GATE_READY("Connection SCSGate is Ready");

	private String description = null;

	SCSGateState(String desc) {
        this.description = desc;
    }

    public String getDescription() {
        return this.description;
    }

	private static SCSGateState[] vals = values();

	public SCSGateState next() {
		return vals[(this.ordinal()+1) % vals.length];
	}
}
