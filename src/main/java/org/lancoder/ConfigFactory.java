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
import org.lancoder.common.exceptions.InvalidConfiguration;

import com.google.gson.Gson;

public class ConfigFactory<T extends Config> {

	private Class<T> clazz;
	private T instance;

	public ConfigFactory(Class<T> clazz) throws InvalidConfiguration {
		this(clazz, null);
	}

	public ConfigFactory(Class<T> clazz, String configPath) throws InvalidConfiguration {
		try {
			this.clazz = clazz;
			this.instance = clazz.newInstance();
			this.instance.setConfigPath(configPath == null ? instance.getDefaultPath() : configPath);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new InvalidConfiguration();
		}
	}

	/**
	 * Initialize a new configuration and save new configuration to disk.
	 * 
	 * @param userInput
	 *            If true, the method will get data from System.in (the user). Otherwise loads default values.
	 * @return The new configuration
	 */
	public T init(boolean userInput) {
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
	 * @throws InvalidConfiguration
	 *             If file is corrupted or missing
	 */
	public T load() throws InvalidConfiguration {
		if (!Files.exists(Paths.get(instance.getConfigPath()))) {
			throw new InvalidConfiguration();
		}
		try {
			byte[] b = Files.readAllBytes(Paths.get(instance.getConfigPath()));
			T config = fromJson(new String(b, "UTF-8"));
			config.setConfigPath(instance.getConfigPath());
			System.out.println("Loaded config from disk");
			return config;
		} catch (IOException e) {
			throw new InvalidConfiguration();
		}
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

	private T fromJson(String jsonString) {
		Gson gson = new Gson();
		return gson.fromJson(jsonString, this.clazz);
	}

}
