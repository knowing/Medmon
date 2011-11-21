package de.lmu.ifi.dbs.medmon.sensor.core.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeUtil {

	public static int getCalendarConstant(Date firstTimestamp, Date lastTimestamp) {
		// Lazy initialize calendarConstant
		int calendarConstant = -1;
		if (firstTimestamp != null && lastTimestamp != null) {
			Calendar first = new GregorianCalendar();
			Calendar last = new GregorianCalendar();
			first.setTime(firstTimestamp);
			last.setTime(lastTimestamp);

			// Checking the Calendar Constant: General -> Detail
			if (first.get(Calendar.WEEK_OF_YEAR) == last.get(Calendar.WEEK_OF_YEAR)) // Same
																						// Week
				calendarConstant = Calendar.WEEK_OF_YEAR;
			if (first.get(Calendar.DAY_OF_YEAR) == last.get(Calendar.DAY_OF_YEAR)) // Same
																					// Day
				calendarConstant = Calendar.DAY_OF_YEAR;
			if (first.get(Calendar.HOUR_OF_DAY) == last.get(Calendar.HOUR_OF_DAY)) // Same
																					// Hour
				calendarConstant = Calendar.HOUR_OF_DAY;
		}
		return calendarConstant;
	}
}
