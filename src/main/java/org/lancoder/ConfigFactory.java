package org.lancoder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.lancoder.common.Config;
import org.lancoder.common.exceptions.MissingConfiguration;

import com.google.gson.Gson;

public class ConfigFactory<T extends Config> {

	private Class<T> clazz;

	public ConfigFactory(Class<T> clazz) {
		this.clazz = clazz;
	}

	private HashMap<Field, String> getFields() {
		HashMap<Field, String> fields = new HashMap<>();
		for (Field field : clazz.getDeclaredFields()) {
			Prompt p = field.getAnnotation(Prompt.class);
			if (p != null) {
				fields.put(field, p.message());
			}
		}
		return fields;
	}

	private T promptUser() {
		T config = null;
		try (Scanner s = new Scanner(new CloseShieldInputStream(System.in))) {
			s.useDelimiter(System.lineSeparator());
			config = clazz.newInstance();
			HashMap<Field, String> fields = getFields();
			for (Entry<Field, String> entry : fields.entrySet()) {
				Field field = entry.getKey();
				String message = entry.getValue();
				field.setAccessible(true);
				System.out.printf("%s [%s]: ", message, field.get(config));
				String input = s.nextLine();
				if (!input.isEmpty()) {
					if (field.getType() == java.lang.Integer.TYPE) {
						field.set(config, Integer.valueOf(input));
					} else {
						field.set(config, input);
					}
				}
			}
			s.close();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return config;
	}

	/**
	 * Generate default config instance
	 * 
	 * @return The default config
	 */
	private T generate() {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Initialize a new configuration and save new configuration to disk with default path.
	 * 
	 * @param clazz
	 *            The type of configuration
	 * @param userInput
	 *            If true, the method will get data from System.in (the user). Otherwise loads default values.
	 * @return The new configuration
	 */
	public T init(boolean userInput) {
		T config = null;
		try {
			config = clazz.newInstance();
			return init(config.getDefaultPath(), userInput);
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
	}

	/**
	 * Initialize a new configuration and save new configuration to disk.
	 * 
	 * @param configPath
	 *            The path of the configuration
	 * @param clazz
	 *            The type of configuration
	 * @param userInput
	 *            If true, the method will get data from System.in (the user). Otherwise loads default values.
	 * @return The new configuration
	 */
	public T init(String configPath, boolean userInput) {
		T config = userInput ? promptUser() : generate();
		config.setConfigPath(configPath);
		config.dump();
		return config;
	}

	/**
	 * Load configuration from disk from the default class path.
	 * 
	 * @param clazz
	 *            The type of configuration
	 * @return The loaded configuration
	 * @throws MissingConfiguration
	 *             If file is corrupted or missing
	 */
	public T load() throws MissingConfiguration {
		T config = generate();
		return load(config.getDefaultPath());
	}

	/**
	 * Load configuration from disk from the provided path.
	 * 
	 * @param configPath
	 *            The path of the configuration
	 * @param clazz
	 *            The type of configuration
	 * @return The loaded configuration
	 * @throws MissingConfiguration
	 *             If file is corrupted or missing
	 */
	public T load(String configPath) throws MissingConfiguration {
		T config = generate();
		if (!Files.exists(Paths.get(configPath))) {
			throw new MissingConfiguration();
		}
		try {
			byte[] b = Files.readAllBytes(Paths.get(configPath));
			config = fromJson(new String(b, "UTF-8"));
			config.setConfigPath(configPath);
			System.out.println("Loaded config from disk");
		} catch (IOException e) {
			throw new MissingConfiguration();
		}
		return config;
	}

	private T fromJson(String jsonString) {
		Gson gson = new Gson();
		return gson.fromJson(jsonString, this.clazz);
	}
}
