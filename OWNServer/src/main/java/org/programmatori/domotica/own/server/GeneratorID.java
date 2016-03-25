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
package org.programmatori.domotica.own.server;

import java.util.Calendar;

/**
 * Generate a id for any client connection
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since TCPIPServer v0.1.1
 * @version 1.0, 22/02/2013
 */
public class GeneratorID {

	/**
	 * Generate a id for every client
	 */
	public static synchronized long get() {
		return Calendar.getInstance().getTimeInMillis();
	}
}
