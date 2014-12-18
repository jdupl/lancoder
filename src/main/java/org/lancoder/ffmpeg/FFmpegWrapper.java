package org.lancoder.ffmpeg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.file_components.FileInfo;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.common.third_parties.FFprobe;
import org.lancoder.ffmpeg.probers.CodecProber;
import org.lancoder.ffmpeg.probers.FileProber;
import org.lancoder.ffmpeg.probers.VersionProber;

public class FFmpegWrapper {

	public static ArrayList<CodecEnum> getAvailableCodecs(FFmpeg module) {
		CodecProber prober = new CodecProber();
		return prober.getNodeCapabilities(module);
	}

	public static FileInfo getFileInfo(File absoluteFile, String relativePath, FFprobe module) {
		FileProber prober = new FileProber();
		return prober.getFileInfo(absoluteFile, relativePath, module);
	}

	public static HashMap<String, String> getVersions(FFmpeg module) {
		VersionProber prober = new VersionProber();
		return prober.getVersions(module);
	}
}
