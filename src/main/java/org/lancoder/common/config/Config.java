package org.lancoder.common.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class Config {

	protected static final String DEFAULT_FFMPEG_PATH = "ffmpeg";

	protected transient String configPath;

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

}
