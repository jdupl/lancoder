package org.lancoder.common.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.lancoder.common.exceptions.InvalidConfigurationException;
import org.yaml.snakeyaml.Yaml;

public class ConfigManager<T extends Config> {

	private static final String CONF_NOT_FOUND = "Cannot load configuration file %s.%n"
			+ "Initialize a configuration file with --init options.";
	private static final String CONF_CORRUPTED = "Cannot load configuration file %s.%n"
			+ "Looks like the file is corrupted.%n " + "Please initialize a new config and overwrite current config.";

	private Class<? extends Config> clazz;
	private T config;
	private String configPath;

	public ConfigManager(Class<T> clazz, String configPath) {
		this.configPath = configPath;
		this.clazz = clazz;

		if (configPath == null) {
			try {
				this.configPath = clazz.newInstance().getDefaultPath();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public ConfigManager(T config, String configPath) {
		this.configPath = configPath;
		this.config = config;
		this.clazz = config.getClass();
	}

	/**
	 * Load configuration from disk from the provided path.
	 * 
	 * @return The loaded configuration
	 * @throws InvalidConfigurationException
	 *             If file is corrupted or missing
	 */
	@SuppressWarnings("unchecked")
	public boolean load() throws InvalidConfigurationException {
		if (!Files.exists(Paths.get(this.configPath))) {
			throw new InvalidConfigurationException(String.format(CONF_NOT_FOUND, this.configPath));
		}

		try {
			Logger logger = Logger.getLogger("lancoder");

			FileInputStream fis = new FileInputStream(this.configPath);
			Yaml yaml = new Yaml();
			this.config = (T) yaml.loadAs(fis, this.clazz);

			logger.fine("Loaded config from disk");
			return true;
		} catch (IOException | NullPointerException e) {
			throw new InvalidConfigurationException(String.format(CONF_CORRUPTED, this.configPath));
		}
	}

	/**
	 * Serializes current config to disk as YAML object.
	 * 
	 * @return True if could write config to disk. Otherwise, return false.
	 */
	public synchronized boolean dump() {
		File configFile = new File(configPath);
		Yaml yaml = new Yaml();

		String s = yaml.dumpAsMap(this.config);

		try {
			if (!configFile.exists()) {
				configFile.getParentFile().mkdirs();
			}
			Files.write(Paths.get(configPath), s.getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public T getConfig() {
		return config;
	}

	public void setConfig(T config) {
		this.config = config;
	}

	public void setConfigPath(String path) {
		this.configPath = path;
	}

	public String getConfigPath() {
		return configPath;
	}

}