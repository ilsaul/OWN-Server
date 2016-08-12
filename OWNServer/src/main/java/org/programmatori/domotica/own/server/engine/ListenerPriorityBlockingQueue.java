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
package org.programmatori.domotica.own.server.engine;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a {@link PriorityBlockingQueue} that implement listener for chnge data.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.0.1, 27/02/2013
 * @since OWNServer v0.1.1
 */
public class ListenerPriorityBlockingQueue<T> extends PriorityBlockingQueue<T> {
	private static final Logger logger = LoggerFactory.getLogger(ListenerPriorityBlockingQueue.class);
	private static final long serialVersionUID = 8861915379323068946L;

	List<QueueListener> listeners;

	public ListenerPriorityBlockingQueue() {
		super();
		listeners = new ArrayList<QueueListener>();
	}

	@Override
	public T take() throws InterruptedException {
		fireChange();
		return super.take();
	}

	@Override
	public boolean remove(Object o) {
		fireChange();
		boolean ret = super.remove(o);
		logger.debug("queue: {}", size());

		return ret;
	}

	@Override
	public void put(T e) {
		fireChange();
		super.put(e);
		logger.debug("queue: {}", size());
	}

	private void fireChange() {
		for (Iterator<QueueListener> iter = listeners.iterator(); iter.hasNext();) {
			QueueListener listener = (QueueListener) iter.next();

			listener.changeNotify();
		}
	}

	public void addListener(QueueListener listener) {
		listeners.add(listener);
	}

	public void removeListener(QueueListener listener) {
		listeners.remove(listener);
	}
}
