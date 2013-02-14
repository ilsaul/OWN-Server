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

public class Where extends Param {

	public Where(String param) {
	    super(param);
    }

	public int getArea() {
		return (int) getMain() / 10;
	}

	public int getPL() {
		return (getMain() - (getArea() * 10));
	}
	
	public int getLevel() {
		int level = -1;
		
		if (params != null) {
			if (params.size() > 0) {
				level = Integer.valueOf(params.get(0));
			}
		}
		
		return level;
	}
	
	public int getAddress() {
		int address = -1;
		
		if (params != null) {
			if (params.size() > 1) {
				address = Integer.valueOf(params.get(1));
			}
		}
		
		return address;
	}
	
	@Override
	public String toString() {
		String ret = "";
		if (getMain() != -1) {
			ret = super.toString(); 
		}
		return ret;
	}
}
