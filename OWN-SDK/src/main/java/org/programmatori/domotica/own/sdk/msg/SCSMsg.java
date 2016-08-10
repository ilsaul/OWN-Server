/*
 * OWN Server is
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
package org.programmatori.domotica.own.sdk.msg;

import java.io.Serializable;

import org.programmatori.domotica.own.sdk.utils.StringIterator;

/**
 * This class represent the single message of a SCS Bus
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.3.1, 10/08/2016
 */
public class SCSMsg implements Serializable {
	private static final long serialVersionUID = -8822728143247109985L;

	public static final String MSG_STARTER = "*";
	public static final String MSG_SEPARATOR = "*";
	public static final String MSG_ENDER = "##";
	public static final String MSG_FIELD_SEP = "#";
	public static final char MSG_CHAR_STATUS = '#';

	public static SCSMsg MSG_NACK;
	public static SCSMsg MSG_ACK;

	/**
	 * Command not operative / not know
	 */
	public static SCSMsg MSG_NOP;

	/**
	 * Command manage but receiver device don't replay (not exist)
	 */
	public static SCSMsg MSG_RET;

	/**
	 * Command non execute because message collision on bus
	 */
	public static SCSMsg MSG_COLL;

	/**
	 * Command not execute because impossible access to bus
	 */
	public static SCSMsg MSG_NOBUS;

	/**
	 * Command not execute, because interface already busy in transmission
	 */
	public static SCSMsg MSG_USY;

	/**
	 * Procedure multiframe not execute complete
	 */
	public static SCSMsg MSG_PROC;

	static {
	    try {
	    	MSG_NACK  = new SCSMsg("*#*0##");
	    	MSG_ACK   = new SCSMsg("*#*1##");
	    	MSG_NOP   = new SCSMsg("*#*2##");
	    	MSG_RET   = new SCSMsg("*#*3##");
	    	MSG_COLL  = new SCSMsg("*#*4##");
	    	MSG_NOBUS = new SCSMsg("*#*5##");
	    	MSG_USY   = new SCSMsg("*#*6##");
	    	MSG_PROC  = new SCSMsg("*#*7##");
	    } catch ( Exception e ) {
	      // Stub !!
	    }
	  }

	private Who who;
	private Where where;
	private What what;
	private Property property;
	private Value value;
	private boolean statusWho;
	private boolean statusWhere;
	private boolean statusProperty;

	/**
	 * Base Constructor prepare an empty message.
	 */
	private SCSMsg() {
		who = null;
		where = null;
		what = null;
		property = null;
		value = null;
		statusWho = false;
		statusWhere = false;
		statusProperty = false;
	}


	/**
	 * This Constructor create the message from a real message
	 *
	 * @param msg a string that represent the message
	 * @throws MessageFormatException if the message is badly formatted
	 */
	public SCSMsg(String msg) throws MessageFormatException {
		this();

		decode(msg);
    }

	/**
	 * This constructor start from the base component to construct che msg
	 *
	 * @param who
	 * @param where
	 * @param what
	 */
	public SCSMsg(Who who, Where where, What what) {
		this();

		this.who = who;
		this.where = where;
		this.what = what;
	}

	public SCSMsg(Who who, Where where, What what, Property property, Value value) {
		this(who, where, what);

		this.property = property;
		this.value = value;
	}

	public SCSMsg(Who who, boolean statusWho, Where where, What what, Property property, Value value) {
		this(who, where, what);

		this.statusWho = statusWho;
		this.property = property;
		this.value = value;
	}

	private void decode(String msg) throws MessageFormatException {
		// Check if msg is correct
		if (!msg.startsWith(MSG_STARTER)) {
			//throw new RuntimeException("Message Format Error");
			throw new MessageFormatException();
		}

		if (!msg.endsWith(MSG_ENDER)) {
			//throw new RuntimeException("Message Format Error");
			throw new MessageFormatException();
		}

		//StringTokenizer stMsg = new StringTokenizer(msg.substring(0, msg.length() -2), MSG_SEPARATOR);
		StringIterator stMsg2 = new StringIterator(msg.substring(1, msg.length() -2), MSG_SEPARATOR.charAt(0));

		switch (stMsg2.countStrings()) {
		case 2: // Comando Status *WHO*WHERE##
			statusWho = false;

			String tempWho = stMsg2.nextString();
			if (tempWho.charAt(0) == SCSMsg.MSG_CHAR_STATUS) {
				statusWho = true;

				tempWho = tempWho.substring(1);
			}
			who = new Who(tempWho);
			if (stMsg2.hasNext()) where = new Where(stMsg2.nextString());
			break;

		case 3: // If don't start with # then normal command *WHO*WHAT*WHERE## else  *#WHO*WHERE*PROPERTY##
			statusWho = false;
			tempWho = stMsg2.nextString();
			if (tempWho.charAt(0) == SCSMsg.MSG_CHAR_STATUS) {
				statusWho = true;

				tempWho = tempWho.substring(1);
			}
			who = new Who(tempWho);

			if (statusWho) {
				if (stMsg2.hasNext()) where = new Where(stMsg2.nextString());
				if (stMsg2.hasNext()) property = new Property(stMsg2.nextString());
			} else {
				if (stMsg2.hasNext()) what = new What(stMsg2.nextString());
				if (stMsg2.hasNext()) where = new Where(stMsg2.nextString());
			}
			break;

		case 4:
//			who = new Who(getVauleWithStatus(stMsg2.nextString(), statusWho));
			statusWho = false;
			tempWho = stMsg2.nextString();
			if (tempWho.charAt(0) == SCSMsg.MSG_CHAR_STATUS) {
				statusWho = true;

				tempWho = tempWho.substring(1);
			}
			who = new Who(tempWho);
			if (stMsg2.hasNext()) where = new Where(stMsg2.nextString());
			//if (stMsg2.hasNext()) what = new What(stMsg2.nextString());
			if (stMsg2.hasNext()) property = new Property(stMsg2.nextString());
			if (stMsg2.hasNext()) value = new Value(stMsg2.nextString());
			break;

		default:
			statusWho = false;
			tempWho = stMsg2.nextString();
			if (tempWho.length() > 0 && tempWho.charAt(0) == SCSMsg.MSG_CHAR_STATUS) {
				statusWho = true;

				tempWho = tempWho.substring(1);
			}
			who = new Who(tempWho);
			if (stMsg2.hasNext()) {
				statusWhere = false;
				String tempWhere = stMsg2.nextString();

				if (tempWhere.length() > 0 && tempWhere.charAt(0) == SCSMsg.MSG_CHAR_STATUS) {
					statusWhere = true;

					tempWhere = tempWhere.substring(1);
				}
				where = new Where(tempWhere);
				//where = new Where(stMsg2.nextString());
			}
			//if (stMsg2.hasNext()) what = new What(stMsg2.nextString());

			if (stMsg2.hasNext()) {
				String tempProp = stMsg2.nextString();
				if (tempProp.length() > 0 && tempProp.charAt(0) == SCSMsg.MSG_CHAR_STATUS) {
					statusProperty = true;

					tempProp = tempProp.substring(1);
				}
				property = new Property(tempProp);
			}

			while (stMsg2.hasNext()) {
				if (value == null) {
					value = new Value(stMsg2.nextString());
				} else {
					value.addValue(stMsg2.nextString());
				}
			}
		}
	}

	private String encode() {
		StringBuilder sb = new StringBuilder();
		sb.append(MSG_STARTER);
		if (statusWho) sb.append(MSG_CHAR_STATUS);
		if (who != null) sb.append(who.toString());
		if (what != null) sb.append(MSG_SEPARATOR).append(what.toString());

		if (statusWhere) {
			sb.append(MSG_SEPARATOR).append(MSG_CHAR_STATUS);
			if (where != null) sb.append(where.toString());
		} else {
			if (where != null) sb.append(MSG_SEPARATOR).append(where.toString());
		}
		//if (where != null) sb.append(MSG_SEPARATOR).append(where.toString());
		if (statusProperty) {
			sb.append(MSG_SEPARATOR).append(MSG_CHAR_STATUS);
			if (property != null) sb.append(property.toString());
		} else {
			if (property != null) sb.append(MSG_SEPARATOR).append(property.toString());
		}
		if (value != null) sb.append(MSG_SEPARATOR).append(value.toString());
		sb.append(MSG_ENDER);

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
		return statusWho;
	}

	public boolean isStatusProperty() {
		return statusProperty;
	}

	public boolean isAreaMsg() {
		if (getWhere().getPL() == 0) return true;

		return false;
	}

	public boolean isWhoMsg() {
		if (getWhere().getPL() == 0 && getWhere().getArea() == 0) return true;
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((what == null) ? 0 : what.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((where == null) ? 0 : where.hashCode());
		result = prime * result + ((who == null) ? 0 : who.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		SCSMsg other = (SCSMsg) obj;
		if (what == null) {
			if (other.what != null)
				return false;
		} else if (!what.equals(other.what))
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (where == null) {
			if (other.where != null)
				return false;
		} else if (!where.equals(other.where))
			return false;
		if (who == null) {
			if (other.who != null)
				return false;
		} else if (!who.equals(other.who))
			return false;
		return true;
	}
}
