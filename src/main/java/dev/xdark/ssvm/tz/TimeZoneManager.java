package dev.xdark.ssvm.tz;

/**
 * Time one management.
 *
 * @author xDark
 * @see java.util.TimeZone
 */
public interface TimeZoneManager {

	/**
	 * @param javaHome
	 * 		Path to Java home.
	 *
	 * @return platform defined TimeZone id.
	 */
	String getSystemTimeZoneId(String javaHome);

	/**
	 * @return custom time zone id
	 * based on the GMT offset of the platform.
	 */
	String getSystemGMTOffsetId();
}
