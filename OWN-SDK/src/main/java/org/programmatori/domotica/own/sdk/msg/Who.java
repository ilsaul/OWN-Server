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
package org.programmatori.domotica.own.sdk.msg;

/**
 * Part of SCS Message
 * 
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0.0, 21/03/2010
 */
public class Who {
	//public static final String MSG_1 = "Message to manage light";

	private int who;

	public Who(String who) {
		String tempWho = who;
		if (tempWho.trim().length() > 0) {
			this.who = Integer.parseInt(tempWho);
		} else {
			this.who = 0;
		}
    }

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (who != 0) sb.append(Integer.toString(who));

	    return sb.toString();
	}

	public int getMain() {
		return who;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + who;
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
		Who other = (Who) obj;
		if (who != other.who)
			return false;
		return true;
	}
}
