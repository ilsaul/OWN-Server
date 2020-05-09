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
package org.programmatori.domotica.own.sdk.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A String iterator that works as expected when two successive delimiters are
 * found (as opposed to java.util.StringTokenizer, it returns null as the String
 * in that case). Only a single character delimiter is allowed.
 */
public class StringIterator implements Iterator<Object> {

	private final String str;
	private final char delim;
	private int prevPos;
	private int pos;
	private final boolean noFirst;

	public StringIterator(String str, char delim) {
		this(str, delim, false);
	}

	public StringIterator(String str, char delim, boolean noFirst) {
		this.str = str;
		this.delim = delim;

		this.noFirst = noFirst;
		start();
	}

	private void start() {
		prevPos = 0;
		pos = str.indexOf(delim);

		if (noFirst && hasNext()) nextString();
	}

	@Override
	public boolean hasNext() {
		return prevPos != -1;
	}

	@Override
	public Object next() {
		if(!hasNext()){
			throw new NoSuchElementException();
		}

		return nextString();
	}

	public String nextString() {
		if (pos == -1) {
			String token = str.substring(prevPos);
			prevPos = -1;
			return token;
		}
		String token = str.substring(prevPos, pos);
		prevPos = pos + 1;
		pos = str.indexOf(delim, prevPos);
		return token;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public int countStrings() {
		int count = 0;

		while (hasNext()) {
			nextString();
			count++;
		}

		// Restart date
		start();

		return count;
	}

}
