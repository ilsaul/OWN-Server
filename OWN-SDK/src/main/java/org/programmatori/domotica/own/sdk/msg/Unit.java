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
package org.programmatori.domotica.own.sdk.msg;

import org.programmatori.domotica.own.sdk.utils.StringIterator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Basic part of SCS Message.
 * Any unit can be made by a Main value, some parameter and status flag.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 10/08/2016
 */
abstract class Unit implements Serializable {
	private static final long serialVersionUID = -7005336258301327377L;

	/** Separator between element of a unit */
	public static final char PARAM_SEPARATOR = '#';
	/** status symbol in the unit */
	public static final char UNIT_STATUS = '#';

	private int main = -1;
	private String sMain;
	private final List<String> params = new ArrayList<>();
	private boolean status;

	/**
	 * Constructor for SCS String to Object.
	 *
	 * @param param SCS String
	 */
	public Unit(String param) {
		String newParam = param;

		if (statusExist() && param.startsWith(String.valueOf(UNIT_STATUS))) {
				status = true;
				newParam = param.substring(1);
		}

		StringIterator st = new StringIterator(newParam, PARAM_SEPARATOR);

		if (st.hasNext()) {
			sMain = st.nextString();
			if (sMain.trim().length() > 0) {
				main = Integer.parseInt(sMain);
			}
		}

		while (st.hasNext()) {
			this.params.add(st.nextString());
		}
	}

	/**
	 * Constructor for Byte to Object
	 *
	 * @param status of the unit
	 * @param main value of the unit
	 * @param params of the units
	 */
	public Unit(boolean status, String main, String...params) {
		this.status = status;

		sMain = main;
		if (sMain.isEmpty()) {
			this.main = 0;
		} else {
			this.main = Integer.parseInt(sMain);
		}

		this.params.addAll(Arrays.asList(params));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (isStatus()) {
			sb.append(UNIT_STATUS);
		}

		sb.append(sMain);

		for (String param : params) {
			sb.append(PARAM_SEPARATOR).append(param);
		}

		return sb.toString();
	}

	public int getMain() {
		return main;
	}

	public String getMainAsString() {
		return sMain;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Unit unit = (Unit) o;
		return main == unit.main &&
				status == unit.status &&
				Objects.equals(sMain, unit.sMain) &&
				params.equals(unit.params);
	}

	@Override
	public int hashCode() {
		return Objects.hash(main, sMain, params, status);
	}

	/**
	 * Count the value in the params list
	 */
	public int countParams() {
		return params.size();
	}

	/**
	 * One of the parameter
	 *
	 * @param index of params to retrieve
	 * @return the requested parameter
	 */
	public String getParam(int index) {
		return params.get(index);
	}

	/**
	 * If I start from byte I can only add in this why
	 *
	 * @param param to add to the list
	 */
	public void addParam(String param) {
		params.add(param);
	}

	/**
	 * If status flag is set return true
	 *
	 * @return boolean value of status flag
	 */
	public boolean isStatus() {
		return status && statusExist();
	}

	/**
	 * Override with false if the object don't have status
	 *
	 * @return if the object can have a status
	 */
	protected boolean statusExist() {
		return true;
	}

	protected void setMain(int main) {
		this.main = main;
	}
}
