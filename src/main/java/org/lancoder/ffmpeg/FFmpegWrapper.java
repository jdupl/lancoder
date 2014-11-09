package org.lancoder.ffmpeg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.lancoder.common.codecs.Codec;
import org.lancoder.common.config.Config;
import org.lancoder.common.file_components.FileInfo;
import org.lancoder.common.third_parties.FFprobe;
import org.lancoder.ffmpeg.probers.CodecProber;
import org.lancoder.ffmpeg.probers.FileProber;
import org.lancoder.ffmpeg.probers.VersionProber;

public class FFmpegWrapper {

	public static ArrayList<Codec> getAvailableCodecs(Config config) {
		CodecProber prober = new CodecProber();
		return prober.getNodeCapabilities(config);
	}

	public static FileInfo getFileInfo(File absoluteFile, String relativePath, FFprobe fFProbe) {
		FileProber prober = new FileProber();
		return prober.getFileInfo(absoluteFile, relativePath, fFProbe);
	}

	public static HashMap<String, String> getVersions(Config config) {
		VersionProber prober = new VersionProber();
		return prober.getVersions(config);
	}
}
