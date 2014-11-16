package org.lancoder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.lancoder.common.annotations.Prompt;
import org.lancoder.common.config.Config;
import org.lancoder.common.exceptions.InvalidConfigurationException;
import org.lancoder.worker.WorkerConfig;

import com.google.gson.Gson;

public class ConfigFactory<T extends Config> {

	private static final String CONF_NOT_FOUND = "Cannot load configuration file %s.%n"
			+ "Initialize a configuration file with --init options.";
	private static final String CONF_CORRUPTED = "Cannot load configuration file %s.%n"
			+ "Could not cast cast. Looks like the file is corrupted.%n "
			+ "Please initialize a new config and overwrite current config.";
	private static final String CONF_EXISTS = "Configuration file %s exists. Cannot overwrite the file !%n"
			+ "Perhaps you should use the flag --overwrite.";

	private Class<T> clazz;
	private T instance;

	public ConfigFactory(Class<T> clazz) throws InvalidConfigurationException {
		this(clazz, null);
	}

	public ConfigFactory(Class<T> clazz, String configPath) throws InvalidConfigurationException {
		try {
			this.clazz = clazz;
			this.instance = clazz.newInstance();
			this.instance.setConfigPath(configPath == null ? instance.getDefaultPath() : configPath);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new InvalidConfigurationException();
		}
	}

	/**
	 * Initialize a new configuration and save new configuration to disk.
	 * 
	 * @param userInput
	 *            If true, the method will get data from System.in (the user). Otherwise loads default values.
	 * @param overwrite
	 *            If configuration file exists, override it.
	 * @return The new configuration
	 * @throws InvalidConfigurationException
	 *             If file exists and factory must NOT overwrite the file.
	 */
	public T init(boolean userInput, boolean overwrite) throws InvalidConfigurationException {
		File f = new File(instance.getConfigPath());
		if (!overwrite && f.exists()) {
			throw new InvalidConfigurationException(String.format(CONF_EXISTS, f.getAbsoluteFile()));
		}
		if (instance instanceof WorkerConfig) {
			// Hackish way to retrieve localhost only when neccessary
			// Some systems do reverse DNS lookups and may block for quite a long time
			((WorkerConfig) instance).setNameFromHostName();
		}
		T config = (userInput ? promptUser() : instance);
		config.dump();
		return config;
	}

	/**
	 * Load configuration from disk from the provided path.
	 * 
	 * @param clazz
	 *            The type of configuration
	 * @return The loaded configuration
	 * @throws InvalidConfigurationException
	 *             If file is corrupted or missing
	 */
	public T load() throws InvalidConfigurationException {
		if (!Files.exists(Paths.get(instance.getConfigPath()))) {
			throw new InvalidConfigurationException(String.format(CONF_NOT_FOUND, instance.getConfigPath()));
		}
		try {
			byte[] b = Files.readAllBytes(Paths.get(instance.getConfigPath()));
			T config = fromJson(new String(b, "UTF-8"));
			config.setConfigPath(instance.getConfigPath());
			System.out.println("Loaded config from disk");
			return config;
		} catch (IOException e) {
			throw new InvalidConfigurationException(String.format(CONF_CORRUPTED, instance.getConfigPath()));
		}
	}

	private T promptUser() {
		System.out.println("Please enter the following fields. Default values are in [].");
		System.out.println("To use the default value, hit return.");
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
			config.setConfigPath(instance.getConfigPath());
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return config;
	}

	private HashMap<Field, String> getFields() {
		HashMap<Field, String> fields = new HashMap<>();
		fields.putAll(getFieldsFromClass(clazz));
		fields.putAll(getFieldsFromClass(clazz.getSuperclass()));
		return fields;
	}

	private HashMap<Field, String> getFieldsFromClass(Class<?> clazz) {
		HashMap<Field, String> fields = new HashMap<>();
		for (Field field : clazz.getDeclaredFields()) {
			Prompt p = field.getAnnotation(Prompt.class);
			if (p != null) {
				fields.put(field, p.message());
			}
		}
		return fields;
	}

	private T fromJson(String jsonString) {
		Gson gson = new Gson();
		return gson.fromJson(jsonString, this.clazz);
	}

}
