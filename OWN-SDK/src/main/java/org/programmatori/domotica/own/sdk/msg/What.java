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

/**
 * Part of SCS Message.
 * Value of the message
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0.1, 10/08/2016
 */
public class What extends Unit implements Serializable {
	private static final long serialVersionUID = 4206270180792686072L;

	public What(String param) {
	    super(param);
    }

    public What(String main, String...params) {
		super(false, main, params);
    }

	/**
	 * Force to false the status
	 * @return
	 */
	@Override
	protected boolean statusExist() {
		return false;
	}
}
