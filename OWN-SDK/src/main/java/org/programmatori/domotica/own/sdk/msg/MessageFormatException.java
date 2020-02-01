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

/**
 * This error occurred when the SCS Message is not well formatted
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 21/03/2010
 */
public class MessageFormatException extends RuntimeException {
	private static final long serialVersionUID = 1584054744811135388L;

	public MessageFormatException() {
		super("Message Format Error");
	}

	public MessageFormatException(String msg) {
		super(msg);
	}
}
