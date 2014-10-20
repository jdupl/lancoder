package org.lancoder.ffmpeg.probers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lancoder.common.codecs.Codec;
import org.lancoder.common.config.Config;
import org.lancoder.common.exceptions.MissingFfmpegException;
import org.lancoder.ffmpeg.FFmpegReader;
import org.lancoder.ffmpeg.FFmpegReaderListener;

public class CodecProber implements FFmpegReaderListener {

	private static Pattern codecPattern = Pattern.compile("[VASFSXBD\\s\\.]{6,8}([\\p{Lower}\\d\\-]+)");
	private final ArrayList<Codec> codecs = new ArrayList<>();

	/**
	 * Return a list of supported codecs from the current system.
	 * 
	 * @return
	 */
	public ArrayList<Codec> getNodeCapabilities(Config config) {
		FFmpegReader reader = new FFmpegReader();
		ArrayList<String> args = new ArrayList<>();
		args.add(config.getFFmpegPath());
		args.add("-encoders");
		try {
			reader.read(args, this, false);
		} catch (MissingFfmpegException e) {
			e.printStackTrace();
		}
		return codecs;
	}

	@Override
	public void onMessage(String line) {
		Matcher m = codecPattern.matcher(line);
		if (m.find() && m.groupCount() == 1) {
			String s = m.group(1);
			Codec c = Codec.findByLib(s);
			if (c != Codec.UNKNOWN) {
				codecs.add(c);
			}
		}
	}
}
