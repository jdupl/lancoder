package org.lancoder.ffmpeg.probers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lancoder.common.config.Config;
import org.lancoder.common.exceptions.MissingFfmpegException;
import org.lancoder.ffmpeg.FFmpegReader;
import org.lancoder.ffmpeg.FFmpegReaderListener;

public class VersionProber implements FFmpegReaderListener {

	private final static Pattern ffmpegVersionPattern = Pattern.compile("^ffmpeg version ([0-9]+\\.[0-9]+\\.[0-9]+)");
	private final static Pattern libVersionPattern = Pattern
			.compile("^(lib[a-z]+)\\s*([0-9]+).\\s*([0-9]+).\\s*([0-9]+)");
	private final HashMap<String, String> versions = new HashMap<>();

	public HashMap<String, String> getVersions(Config config) {
		FFmpegReader ffmpeg = new FFmpegReader();
		ArrayList<String> args = new ArrayList<>();
		args.add(config.getFFmpegPath());
		args.add("-version");
		try {
			ffmpeg.read(args, this, false);
		} catch (MissingFfmpegException e) {
			e.printStackTrace();
		}
		return versions;
	}

	@Override
	public void onMessage(String line) {
		Matcher m = ffmpegVersionPattern.matcher(line);
		if (m.find()) {
			versions.put("ffmpeg", m.group(1));
		} else if ((m = libVersionPattern.matcher(line)).find() && m.groupCount() == 4) {
			versions.put(m.group(1), String.format("%s.%s.%s", m.group(2), m.group(3), m.group(4)));
		}
	}
}
