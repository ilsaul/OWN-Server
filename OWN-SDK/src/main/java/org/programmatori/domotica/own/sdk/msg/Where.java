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

/**
 * Part of SCS Message.
 * Destination of the message
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 10/08/2016
 */
public class Where extends Unit implements Serializable {
	private static final long serialVersionUID = -3912228509235975203L;

	public Where(String where) {
		// 00 have no meaning because general use 0 only
		super(where.equals("00")? "0" : where);


	}

	public Where(boolean status, String main, String...params) {
		super(status, main, params);
	}

	public int getArea() {
		if (getMain() > 9) {
			return (int) getMain() / 10;
		} else {
			return getMain();
		}
	}

	public int getPL() {
		if (getMain() > 9) {
			return (getMain() - (getArea() * 10));
		} else {
			return 0;
		}
	}

	public int getLevel() {
		int level = -1;

		if (params != null && params.isEmpty()) {
				level = Integer.valueOf(params.get(0));
		}

		return level;
	}

	public int getAddress() {
		int address = -1;

		if (params != null && params.size() > 1) {
				address = Integer.valueOf(params.get(1));
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
