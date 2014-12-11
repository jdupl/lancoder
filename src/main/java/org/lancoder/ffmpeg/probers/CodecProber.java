package org.lancoder.ffmpeg.probers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.exceptions.MissingThirdPartyException;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.ffmpeg.FFmpegReader;
import org.lancoder.ffmpeg.FFmpegReaderListener;

public class CodecProber implements FFmpegReaderListener {

	private static Pattern codecPattern = Pattern.compile("[VASFSXBD\\s\\.]{6,8}([\\p{Lower}\\d\\-]+)");
	private final ArrayList<CodecEnum> codecs = new ArrayList<>();

	/**
	 * Return a list of supported codecs from the current system.
	 * 
	 * @return
	 */
	public ArrayList<CodecEnum> getNodeCapabilities(FFmpeg module) {
		FFmpegReader reader = new FFmpegReader();
		ArrayList<String> args = new ArrayList<>();
		args.add(module.getPath());
		args.add("-encoders");
		try {
			reader.read(args, this, false);
		} catch (MissingThirdPartyException e) {
			e.printStackTrace();
		}
		return codecs;
	}

	@Override
	public void onMessage(String line) {
		Matcher m = codecPattern.matcher(line);
		if (m.find() && m.groupCount() == 1) {
			String s = m.group(1);
			CodecEnum c = CodecEnum.findByLib(s);
			if (c != CodecEnum.UNKNOWN) {
				codecs.add(c);
			}
		}
	}
}
