package org.lancoder.ffmpeg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.lancoder.common.codecs.Codec;
import org.lancoder.common.file_components.FileInfo;
import org.lancoder.ffmpeg.probers.CodecProber;
import org.lancoder.ffmpeg.probers.FileProber;
import org.lancoder.ffmpeg.probers.VersionProber;

public class FFmpegWrapper {

	public static ArrayList<Codec> getAvailableCodecs() {
		CodecProber prober = new CodecProber();
		return prober.getNodeCapabilities();
	}

	public static FileInfo getFileInfo(File absoluteFile, String relativePath) {
		FileProber prober = new FileProber();
		return prober.getFileInfo(absoluteFile, relativePath);
	}

	public static HashMap<String, String> getVersions() {
		VersionProber prober = new VersionProber();
		return prober.getVersions();
	}
}
