package dev.xdark.ssvm.tz;

import lombok.val;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Basic time zone manager implementation.
 *
 * @author xDark
 */
public class SimpleTimeZoneManager implements TimeZoneManager {

	@Override
	public String getSystemTimeZoneId(String javaHome) {
		return getSystemTimeZone().getID();
	}

	@Override
	public String getSystemGMTOffsetId() {
		val tz = TimeZone.getDefault();
		val cal = GregorianCalendar.getInstance(tz);
		int offsetInMillis = tz.getOffset(cal.getTimeInMillis());

		val offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
		return "GMT" + (offsetInMillis >= 0L ? "+" : "-") + offset;
	}

	/**
	 * @return system time zone.
	 */
	protected TimeZone getSystemTimeZone() {
		return TimeZone.getDefault();
	}
}
