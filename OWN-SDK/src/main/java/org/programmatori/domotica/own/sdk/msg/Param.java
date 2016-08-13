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
package org.programmatori.domotica.own.sdk.msg;

import java.io.Serializable;
import java.util.*;

/**
 * Part of SCS Message
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0.1, 10/08/2016
 */
public class Param implements Serializable {
	private static final long serialVersionUID = -7005336258301327377L;

	public static final String PARAM_SEPARATOR = "#";

	private int main = -1;
	private String sMain;
	ArrayList<String> params = new ArrayList<String>();

	public Param(String param) {
		StringTokenizer st = new StringTokenizer(param, "#");

		if (st.hasMoreTokens()) {
			sMain = st.nextToken();
			if (sMain.trim().length() > 0) {
				main = Integer.parseInt(sMain);
			}
		}

		while (st.hasMoreTokens()) {
			this.params.add(st.nextToken());

		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(sMain);

		for (Iterator<String> iter = params.iterator(); iter.hasNext();) {
			String param = (String) iter.next();
			sb.append(PARAM_SEPARATOR).append(param);
		}

		return sb.toString();
	}

	public int getMain() {
		return main;
	}

	public String getSMain() {
		return sMain;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + main;
		result = prime * result + ((params == null) ? 0 : params.hashCode());
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
		Param other = (Param) obj;

		//13/08/2016 With system getTime() can create a SCSMsg with sMain to null
		if (sMain == null) {
			if (other.sMain != null)
				return false;
		} else if (!sMain.equals(other.sMain)) // Compare sMain because can be different
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		return true;
	}

	/**
	 * Count the value in the params list
	 */
	public int countParams() {
		return params.size();
	}

	public String getParams(int i) {
		return params.get(i);
	}
}
