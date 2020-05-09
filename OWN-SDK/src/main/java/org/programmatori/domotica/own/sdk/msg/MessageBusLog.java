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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This logger Must use olny for log information that arrive on the bus.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 0.1, 13/01/2019
 */
public class MessageBusLog {
	private static final Logger logger = LoggerFactory.getLogger(MessageBusLog.class);

	/**
	 * Log Message SCSMsg
	 */
	public void log(SCSMsg msg, boolean isSend, Long id) {
		String direction = (isSend? "TX MSG:" : "RX MSG:");

		logger.info("{} - {}{}", id, direction, msg);
	}

	/**
	 * Log basic Message
	 */
	public void log(String msg) {
		logger.info("{}", msg);
	}

}
