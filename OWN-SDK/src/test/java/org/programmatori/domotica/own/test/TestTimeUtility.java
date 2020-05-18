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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with OWN Server. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.programmatori.domotica.own.test;

import org.junit.Test;
import org.programmatori.domotica.own.sdk.utils.TimeUtility;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;

public class TestTimeUtility {

	public TestTimeUtility() {
		// stub
	}

	@Test
	public void testTimeDiff() {
		final Calendar cal1 = GregorianCalendar.getInstance();
		final Calendar cal2 = GregorianCalendar.getInstance();
		cal2.setTimeInMillis(cal1.getTimeInMillis());

		cal1.add(Calendar.SECOND, 1);
		final long l = TimeUtility.timeDiff(cal1, cal2);

		assertEquals("La funzione timeDiff sbaglia", 1000, l);
	}

	@Test
	public void testTimeAdd() {
		final Calendar cal1 = GregorianCalendar.getInstance();
		final Calendar cal2 = GregorianCalendar.getInstance();
		cal2.setTimeInMillis(cal1.getTimeInMillis());

		cal1.add(Calendar.SECOND, -1);
		long l = TimeUtility.timeDiff(cal1, cal2);
		l *= -1; // I need add the second I removed then I remove the negative sign.

		assertEquals(cal2.getTimeInMillis(), TimeUtility.timeAdd(l, cal1).getTimeInMillis());
	}
}
