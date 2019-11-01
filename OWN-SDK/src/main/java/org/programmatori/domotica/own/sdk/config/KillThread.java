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
package org.programmatori.domotica.own.sdk.config;

import java.util.Iterator;
import java.util.List;

/**
 * Thread Killer
 *
 * @version 0.1, 19/03/2012
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 */
public class KillThread extends Thread {
	private List<Thread> list;

	public KillThread(List<Thread> listThread) {
		list = listThread;
	}

	@Override
	public void run() {
		try {
			sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (Iterator<Thread> iter = list.iterator(); iter.hasNext();) {
			Thread t = iter.next();

			if (t.isAlive()) {
				//t.interrupt();
			}
		}
	}

}
