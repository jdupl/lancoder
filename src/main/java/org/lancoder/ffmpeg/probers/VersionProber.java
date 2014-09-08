package org.lancoder.ffmpeg.probers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lancoder.common.exceptions.MissingDecoderException;
import org.lancoder.common.exceptions.MissingFfmpegException;
import org.lancoder.common.exceptions.WorkInterruptedException;
import org.lancoder.ffmpeg.FFmpegReader;
import org.lancoder.ffmpeg.FFmpegReaderListener;

public class VersionProber implements FFmpegReaderListener {

	private final static Pattern ffmpegVersionPattern = Pattern.compile("^ffmpeg version ([0-9]+\\.[0-9]+\\.[0-9]+)");
	private final static Pattern libVersionPattern = Pattern
			.compile("^(lib[a-z]+)\\s*([0-9]+).\\s*([0-9]+).\\s*([0-9]+)");
	private final HashMap<String, String> versions = new HashMap<>();

	public HashMap<String, String> getVersions() {
		FFmpegReader ffmpeg = new FFmpegReader();
		ArrayList<String> args = new ArrayList<>();
		args.add("ffmpeg");
		args.add("-version");
		try {
			ffmpeg.read(args, this, false);
		} catch (WorkInterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MissingDecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MissingFfmpegException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return versions;
	}

	@Override
	public void onMessage(String line) throws MissingDecoderException {
		Matcher m = ffmpegVersionPattern.matcher(line);
		if (m.find()) {
			versions.put("ffmpeg", m.group(1));
		} else if ((m = libVersionPattern.matcher(line)).find() && m.groupCount() == 4) {
			versions.put(m.group(1), String.format("%s.%s.%s", m.group(2), m.group(3), m.group(4)));
		}
	}
}
