package org.lancoder.ffmpeg;

import java.util.ArrayList;

import org.lancoder.common.codecs.Codec;
import org.lancoder.ffmpeg.probers.CodecProber;

public class FFmpegWrapper {

	public static ArrayList<Codec> getAvailableCodecs() {
		CodecProber prober = new CodecProber();
		return prober.getNodeCapabilities();
	}
}
