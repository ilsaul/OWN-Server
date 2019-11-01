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
package org.programmatori.domotica.own.sdk.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Utility for manage the Time.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @since 10/08/2016
 */
public final class TimeUtility {
	/** One sencond in Milliseconds. */
	private static final int SECOND = 1000;
	/** One minute in milliseconds. */
	private static final int MINUTE = SECOND * 60;
	/** One hour in milliseconds. */
	private static final int HOUR = MINUTE * 60;
	/** One day in milliseconds. */
	private static final int DAY = HOUR * 24;

	private TimeUtility() {
		throw new IllegalAccessError("Utility class");
	}

	/**
	 * Time difference.
	 * @param date1 Starting time
	 * @param date2 End time
	 * @return the difference between date1 and date2 in milliseconds
	 */
	public static long timeDiff(Calendar date1, Calendar date2) {
		return date1.getTimeInMillis() - date2.getTimeInMillis();
	}

	/**
	 * Add milliseconds to the date1.
	 * @param milliseconds to add
	 * @param date1 starting date
	 * @return Calendar date with time add by milliseconds
	 */
	public static Calendar timeAdd(long milliseconds, Calendar date1) {
		final long days = milliseconds / DAY;
		final long newMilliseconds = milliseconds - days * DAY;

		// I use res for don't change date1
		final Calendar res = Calendar.getInstance();
		res.setTimeInMillis(date1.getTimeInMillis());

		res.add(Calendar.MILLISECOND, (int)newMilliseconds);
		res.add(Calendar.DAY_OF_YEAR, (int)days);

		return res;
	}
}
