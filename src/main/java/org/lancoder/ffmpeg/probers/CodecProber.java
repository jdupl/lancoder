package org.lancoder.ffmpeg.probers;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lancoder.common.codecs.Codec;
import org.lancoder.common.exceptions.MissingDecoderException;
import org.lancoder.common.exceptions.MissingFfmpegException;
import org.lancoder.common.exceptions.WorkInterruptedException;
import org.lancoder.ffmpeg.FFmpegReader;
import org.lancoder.ffmpeg.FFmpegReaderListener;

public class CodecProber implements FFmpegReaderListener {

	private static Pattern codecPattern = Pattern.compile("[VASFSXBD.]{6}\\s(\\p{Alnum}+)");
	private final ArrayList<Codec> codecs = new ArrayList<>();

	/**
	 * Return a list of supported codecs from the current system.
	 * 
	 * @return
	 */
	public ArrayList<Codec> getNodeCapabilities() {
		FFmpegReader reader = new FFmpegReader();
		ArrayList<String> args = new ArrayList<>();
		args.add("ffmpeg");
		args.add("-encoders");
		try {
			reader.read(args, this, false);
		} catch (WorkInterruptedException | MissingDecoderException | MissingFfmpegException e) {
			e.printStackTrace();
		}
		return codecs;
	}

	@Override
	public void onMessage(String line) throws MissingDecoderException {
		Matcher m = codecPattern.matcher(line);
		if (m.find()) {
			Codec c = Codec.findByLib(m.group(1));
			if (c != Codec.UNKNOWN) {
				codecs.add(c);
			}
		}
	}
}
