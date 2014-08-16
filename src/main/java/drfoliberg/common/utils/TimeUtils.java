package drfoliberg.common.utils;

public class TimeUtils {

	/**
	 * Convert ms count to hh:mm:ss.xxx format
	 * 
	 * @param ms
	 *            The ms count to convert
	 * @return The string in the right format for ffmpeg
	 */
	public static String getStringFromMs(long ms) {
		int hours = (int) (ms / (3600 * 1000));
		int remaining = (int) (ms - hours * 3600 * 1000);
		int minutes = (int) (remaining / (60 * 1000));

		remaining -= minutes * 60 * 1000;

		int seconds = remaining / 1000;
		int decimals = remaining % 1000;
		while (decimals % 10 == 0) {
			decimals /= 10;
		}
		return String.format("%02d:%02d:%02d.%d", hours, minutes, seconds, decimals);
	}

	/**
	 * Convert from hh:mm:ss.xxx format to ms count
	 * 
	 * @param time
	 *            The time formatted as a string hh:mm:ss.xxx
	 * @return The ms count or -1 if string is not formatted correctly
	 */
	public static long getMsFromString(String time) {
		String[] times = time.split(":");
		long msCount = -1;
		if (times.length == 3) {
			int hours = Integer.parseInt(times[0]);
			int mins = Integer.parseInt(times[1]);
			double seconds = Double.parseDouble(times[2]);
			msCount = hours * 3600 * 1000;
			msCount += mins * 60 * 1000;
			msCount += seconds * 1000;
		}
		return msCount;
	}
}
