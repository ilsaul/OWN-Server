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
package org.programmatori.domotica.own.sdk.component;

/**
 * Get component type base on Who value.
 *
 * @author Moreno Cattaneo
 * @since 01/02/2020
 */
public enum Who {
    LIGHT(1, "light"),
    BLIND(2, "blind"),
	GATEWAY(13, "gateway");

    private final String name;
    private final int value;

    Who(int value, String name)
    {
        this.value = value;
        this.name = name;
    }

    // getter method
    public int getValue()
    {
        return value;
    }

    public String getName() {
        return name;
    }

    public static Who createByValue(int value) {
		for (Who who : values()) {
			if (who.getValue() == value) {
				return who;
			}
		}

		throw new IllegalStateException("Unexpected value: " + value);
	}
}
