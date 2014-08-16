package drfoliberg.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import drfoliberg.common.file_components.FileInfo;

public class FFmpegProber {

	public static FileInfo getFileInfo(File file) {
		FileInfo fileInfo = null;
		Process process = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("ffprobe", "-v", "quiet", "-print_format", "json", "-show_format",
					"-show_streams", file.getAbsolutePath());
			process = pb.start();
			InputStream stdout = process.getInputStream();
			JsonParser parser = new JsonParser();
			JsonObject pojo = parser.parse(new InputStreamReader(stdout)).getAsJsonObject();
			stdout.close();
			fileInfo = new FileInfo(pojo);
		} catch (IOException e) {
			System.err.printf("Error while probing file %s\n", file.getAbsoluteFile());
			e.printStackTrace();
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
		return fileInfo;
	}

	/**
	 * Get the duration in second of a media file
	 * 
	 * @param filename
	 *            The file to parse
	 * @return The number of seconds or -1 if not found
	 */
	@Deprecated
	public static float getSecondsDuration(String filename) {
		Process process = null;
		boolean found = false;
		try {
			ProcessBuilder pb = new ProcessBuilder("ffprobe", filename);
			process = pb.start();
		} catch (IOException e) {
			return -1;
		}

		InputStream stderr = process.getErrorStream();
		Scanner s = new Scanner(stderr);
		String line = "";

		// Format is like "Duration: 01:24:20.51"
		Pattern durationPattern = Pattern.compile("Duration:" + "\\s*([0-9]{2}):([0-9]{2}):([0-9]{2}\\.[0-9]{2})");

		float totalSeconds = -1;

		while (s.hasNext() && !found) {
			line = s.nextLine();
			Matcher m = durationPattern.matcher(line);
			if (m.find()) {
				found = true;
				int hours = Integer.parseInt(m.group(1));
				int minutes = Integer.parseInt(m.group(2));
				float seconds = Float.parseFloat(m.group(3));
				totalSeconds = hours * 3600;
				totalSeconds += minutes * 60;
				totalSeconds += seconds;
			}
		}
		s.close();
		return totalSeconds;
	}

	/**
	 * Gets frame rate of (first) video stream
	 * 
	 * @param filename
	 *            The filename to parse
	 * @return The frame per second rate or -1 if not found
	 */
	@Deprecated
	public static float getFrameRate(String filename) {
		boolean found = false;
		Process process = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("ffprobe", filename);
			process = pb.start();
		} catch (IOException e) {
			return -1;
		}

		InputStream stderr = process.getErrorStream();
		Scanner s = new Scanner(stderr);
		String line = "";

		// Format is like "23.98" fps or "25 fps"
		Pattern fpsPattern = Pattern.compile("([0-9]+\\.?[0-9]+)\\s*(fps|tbr)");

		float fps = -1;

		while (s.hasNext() && !found) {
			line = s.nextLine();
			Matcher m = fpsPattern.matcher(line);
			if (m.find()) {
				fps = Float.parseFloat(m.group(1));
				found = true;
			}
		}
		s.close();
		return fps;
	}
}
