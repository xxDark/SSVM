package dev.xdark.ssvm.tz;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Basic time zone manager implementation.
 *
 * @author xDark
 */
public class SimpleTimeManager implements TimeManager {

	@Override
	public String getSystemTimeZoneId(String javaHome) {
		return getSystemTimeZone().getID();
	}

	@Override
	public String getSystemGMTOffsetId() {
		TimeZone tz = getSystemTimeZone();
		Calendar cal = GregorianCalendar.getInstance(tz);
		int offsetInMillis = tz.getOffset(cal.getTimeInMillis());

		String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
		return "GMT" + (offsetInMillis >= 0L ? "+" : "-") + offset;
	}

	@Override
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	@Override
	public long nanoTime() {
		return System.nanoTime();
	}

	/**
	 * @return system time zone.
	 */
	protected TimeZone getSystemTimeZone() {
		return TimeZone.getDefault();
	}
}
