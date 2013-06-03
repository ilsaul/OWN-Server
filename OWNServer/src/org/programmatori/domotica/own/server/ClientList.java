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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Collect all client connection for server
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0, 22/02/2013
 * @since OWNServer v0.1.0
 */
public class ClientList {
	private static final Log log = LogFactory.getLog(ClientList.class);
	private List<ClientConnection> listClient = null;

	public ClientList() {
	    listClient = new ArrayList<ClientConnection>();
    }

	public int add(ClientConnection client) {
		listClient.add(client);
		return listClient.size();
    }

	public int getSize() {
		return listClient.size();
	}

	public void remove(ClientConnection con) {
		listClient.remove(con);
		String name = "Conn #" + con.getId();
		log.debug("Connection removed " + (listClient.size()+1) + ": '" + name + "'");
	}


}
