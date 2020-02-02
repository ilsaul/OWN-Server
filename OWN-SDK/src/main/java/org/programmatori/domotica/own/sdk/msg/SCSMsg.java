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
package org.programmatori.domotica.own.sdk.msg;

import java.io.Serializable;
import java.util.Objects;

import org.programmatori.domotica.own.sdk.utils.StringIterator;

/**
 * This class represent single message of a SCS Bus
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 10/08/2016
 */
public class SCSMsg implements Serializable {
	private static final long serialVersionUID = -8822728143247109985L;

	/** Message starter char. */
	public static final String MSG_STARTER = "*";
	/** Message char separator. */
	public static final String MSG_SEPARATOR = "*";
	/** Message end chars. */
	public static final String MSG_ENDED = "##";

	public static final SCSMsg MSG_NACK = new SCSMsg("*#*0##");
	public static final SCSMsg MSG_ACK = new SCSMsg("*#*1##");

	/** Command not operative / not know */
	public static final SCSMsg MSG_NOP = new SCSMsg("*#*2##");
	/** Command manage but receiver device don't replay (not exist) */
	public static final SCSMsg MSG_RET = new SCSMsg("*#*3##");
	/** Command non execute because message collision on bus */
	public static final SCSMsg MSG_COLL = new SCSMsg("*#*4##");
	/** Command not execute because impossible access to bus */
	public static final SCSMsg MSG_NOBUS = new SCSMsg("*#*5##");
	/** Command not execute, because interface already busy in transmission */
	public static final SCSMsg MSG_BUSY = new SCSMsg("*#*6##");
	/** Procedure multi frame not execute complete */
	public static final SCSMsg MSG_PROC = new SCSMsg("*#*7##");

	private Who who = null;
	private Where where = null;
	private What what = null;
	private Property property = null;
	private Value value = null;

	/**
	 * This Constructor creates the message from a real message
	 *
	 * @param msg a string that represent the message
	 */
	public SCSMsg(String msg) {
		this.decode(msg);
	}

	/**
	 * This constructor start from the base component to construct che msg
	 *
	 * @param who Who
	 * @param where Where
	 * @param what What
	 */
	public SCSMsg(Who who, Where where, What what) {
		this(who, where, what,null,null);
	}

	public SCSMsg(Who who, Where where, What what, Property property, Value value) {
		this.who = who;
		this.where = where;
		this.what = what;
		this.property = property;
		this.value = value;

		if (who.isStatus() && what != null && what.getMain() != 0) {
			throw new MessageFormatException();
		}
	}

	private void decode(String msg) {
		// Check if msg is correct
		if (!msg.startsWith(MSG_STARTER)) {
			throw new MessageFormatException();
		}

		if (!msg.endsWith(MSG_ENDED)) {
			throw new MessageFormatException();
		}

		StringIterator siMsg = new StringIterator(msg.substring(1, msg.length() -2), MSG_SEPARATOR.charAt(0));
		int count = siMsg.countStrings();

		who = new Who(siMsg.nextString());
		if (count == 3 && !who.isStatus() && siMsg.hasNext()) what = new What(siMsg.nextString());
		if (siMsg.hasNext()) where = new Where(siMsg.nextString());
		if (who.isStatus() && siMsg.hasNext()) property = new Property(siMsg.nextString());

		while (siMsg.hasNext()) {
			if (value == null) {
				value = new Value(siMsg.nextString());
			} else {
				value.addValue(siMsg.nextString());
			}
		}
	}

	private String encode() {
		StringBuilder sb = new StringBuilder();
		sb.append(MSG_STARTER);
		if (who != null) sb.append(who);
		if (what != null) sb.append(MSG_SEPARATOR).append(what);
		if (where != null) sb.append(MSG_SEPARATOR).append(where);
		if (property != null) sb.append(MSG_SEPARATOR).append(property);
		if (value != null) sb.append(MSG_SEPARATOR).append(value);
		sb.append(MSG_ENDED);

		return sb.toString();
	}

	@Override
	public String toString() {
		return encode();
	}

	public Who getWho() {
	    return who;
    }

	public Where getWhere() {
		return where;
	}

	public What getWhat() {
		return what;
	}

	public Property getProperty() {
		return property;
	}

	public Value getValue() {
		return value;
	}

	public boolean isStatus() {
		return who.isStatus();
	}

	public boolean isStatusProperty() {
		return property.isStatus();
	}

	public boolean isAreaMsg() {
		return getWhere().getPL() == 0;
	}

	public boolean isWhoMsg() {
		return getWhere().getPL() == 0 && getWhere().getArea() == 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SCSMsg scsMsg = (SCSMsg) o;
		return Objects.equals(who, scsMsg.who) &&
				Objects.equals(where, scsMsg.where) &&
				Objects.equals(what, scsMsg.what) &&
				Objects.equals(property, scsMsg.property) &&
				Objects.equals(value, scsMsg.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(who, where, what, property, value);
	}
}
