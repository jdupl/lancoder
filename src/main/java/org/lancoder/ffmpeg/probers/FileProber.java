package org.lancoder.ffmpeg.probers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.lancoder.common.file_components.FileInfo;
import org.lancoder.common.third_parties.FFprobe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FileProber {

	public FileInfo getFileInfo(File absoluteFile, String relativePath, FFprobe module) {
		FileInfo fileInfo = null;
		Process process = null;
		try {
			ProcessBuilder pb = new ProcessBuilder(module.getPath(), "-v", "quiet", "-print_format", "json",
					"-show_format", "-show_streams", absoluteFile.getPath());
			process = pb.start();
			InputStream stdout = process.getInputStream();
			JsonParser parser = new JsonParser();
			JsonObject json = parser.parse(new InputStreamReader(stdout)).getAsJsonObject();
			stdout.close();
			fileInfo = new FileInfo(json, relativePath);
		} catch (IOException e) {
			Logger logger = Logger.getLogger("lancoder");
			logger.warning(String.format("Error while probing file %s\n", absoluteFile.getAbsoluteFile()));
			logger.warning(e.getMessage());
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
		return fileInfo;
	}
}
