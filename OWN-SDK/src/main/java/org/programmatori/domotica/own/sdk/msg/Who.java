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
 * Part of SCS Message.
 * Type of Element it referring
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 14/08/2016
 */
public class Who extends Unit {
	private static final long serialVersionUID = 1571092377962443998L;

	public Who(String who) {
		super(who);

		if (getSMain().isEmpty()) setMain(0); //ToDo: check it

//		String tempWho = who;
//		if (tempWho.trim().length() > 0) {
//			this.who = Integer.parseInt(tempWho);
//		} else {
//			this.who = 0;
//		}
    }

    public Who(boolean status, String who) {
		super(status, who);
    }
}
