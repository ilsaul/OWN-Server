/*
 * OWN Server is
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
package org.programmatori.domotica.own.sdk.utils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility for manage the Time.
 *
 * @author Moreno Cattaneo (moreno.cattaneo@gmail.com)
 * @version 1.1, 10/08/2016
 */
public class TimeUtility {
	private static final int SECOND = 1000;
	private static final int MINUTE = SECOND * 60;
	private static final int HOUR = MINUTE * 60;
	private static final int DAY = HOUR * 24;

	public static long timeDiff(Calendar date1, Calendar date2) {
		long diff = date1.getTimeInMillis() - date2.getTimeInMillis();

		return diff;
	}

	public static Calendar timeAdd(long milliseconds, Calendar date1) {
		long days = milliseconds / DAY;
		long newMilliseconds = milliseconds - days * DAY;

		date1.add(Calendar.MILLISECOND, (int)newMilliseconds);
		//System.out.println("ms: " + milliseconds);
		date1.add(Calendar.DAY_OF_YEAR, (int)days);
		//System.out.println("day: " + days);

		return date1;
	}

	public static void main(String[] args) {
		Calendar cal1 = GregorianCalendar.getInstance();
		Calendar cal2 = GregorianCalendar.getInstance();
		cal1.add(Calendar.SECOND, 1);
		cal1.add(Calendar.MINUTE, 1);
		cal1.add(Calendar.HOUR_OF_DAY, 1 +1);
		cal1.add(Calendar.DAY_OF_YEAR, 1);
		cal1.add(Calendar.MONTH, 1);
		cal1.add(Calendar.YEAR, 1);

		SimpleDateFormat dateformatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		long l = timeDiff(cal1, cal2);
		System.out.println("system - " + dateformatter.format(cal2.getTime()));
		System.out.println("current - " + dateformatter.format(timeAdd(l, cal2).getTime()));
	}
}
