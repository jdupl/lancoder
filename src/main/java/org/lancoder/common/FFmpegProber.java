package org.lancoder.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lancoder.common.file_components.FileInfo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FFmpegProber {

	public static FileInfo getFileInfo(File file) {
		FileInfo fileInfo = null;
		Process process = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("ffprobe", "-v", "quiet", "-print_format", "json", "-show_format",
					"-show_streams", file.getPath());
			process = pb.start();
			InputStream stdout = process.getInputStream();
			JsonParser parser = new JsonParser();
			JsonObject json = parser.parse(new InputStreamReader(stdout)).getAsJsonObject();
			stdout.close();
			fileInfo = new FileInfo(json);
		} catch (IOException e) {
			System.err.printf("Error while probing file %s\n", file.getAbsoluteFile());
			e.printStackTrace();
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
		return fileInfo;
	}
}
