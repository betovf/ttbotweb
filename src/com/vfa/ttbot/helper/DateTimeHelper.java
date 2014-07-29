package com.vfa.ttbot.helper;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;

public class DateTimeHelper {

	public static Date getDate() {
		return getDate(0);
	}
	
	public static Date getDate(int hoursOffset) {
		// Work with Madrid time zone
		DateTime dtMadrid = new DateTime(DateTimeZone.forID("Europe/Madrid"));
		
		// Apply offset (if zero, returns the same object)
		dtMadrid = dtMadrid.withFieldAdded(DurationFieldType.hours(), hoursOffset);
		
		// Change to local date-time
		return dtMadrid.toLocalDateTime().toDate();
	}
}
