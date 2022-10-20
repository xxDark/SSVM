package dev.xdark.ssvm.timezone;

/**
 * Time management.
 *
 * @author xDark
 * @see java.util.TimeZone
 */
public interface TimeManager {

	/**
	 * @param javaHome Path to Java home.
	 * @return platform defined TimeZone id.
	 */
	String getSystemTimeZoneId(String javaHome);

	/**
	 * @return custom time zone id
	 * based on the GMT offset of the platform.
	 */
	String getSystemGMTOffsetId();

	/**
	 * @return the current time in milliseconds.
	 */
	long currentTimeMillis();

	/**
	 * @return the current time in nanoseconds.
	 */
	long nanoTime();
}
