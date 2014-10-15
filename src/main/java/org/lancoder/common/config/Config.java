package org.lancoder.common.config;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lancoder.common.annotations.Prompt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class Config {

	private static final String DEFAULT_FFMPEG_PATH = InetAddress.getLoopbackAddress().getHostAddress();

	protected transient String configPath;

	@Prompt(message = "FFmpeg's path")
	protected String ffmpegPath;

	public Config() {
		this.ffmpegPath = DEFAULT_FFMPEG_PATH;
	}

	/**
	 * Serializes current config to disk as JSON object.
	 * 
	 * @return True if could write config to disk. Otherwise, return false.
	 */
	public synchronized boolean dump() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String s = gson.toJson(this);
		File config = new File(configPath);
		try {
			if (!config.exists()) {
				config.getParentFile().mkdirs();
			}
			Files.write(Paths.get(configPath), s.getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public abstract String getDefaultPath();

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public String getFFmpegPath() {
		return ffmpegPath;
	}

}
