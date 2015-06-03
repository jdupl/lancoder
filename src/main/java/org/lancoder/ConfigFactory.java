package org.lancoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.lancoder.common.annotations.Prompt;
import org.lancoder.common.config.Config;
import org.lancoder.common.exceptions.InvalidConfigurationException;
import org.lancoder.worker.WorkerConfig;
import org.yaml.snakeyaml.Yaml;

public class ConfigFactory<T extends Config> {

	private static final String CONF_NOT_FOUND = "Cannot load configuration file %s.%n"
			+ "Initialize a configuration file with --init options.";
	private static final String CONF_CORRUPTED = "Cannot load configuration file %s.%n"
			+ "Looks like the file is corrupted.%n " + "Please initialize a new config and overwrite current config.";
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
	 * @return The loaded configuration
	 * @throws InvalidConfigurationException
	 *             If file is corrupted or missing
	 */
	public T load() throws InvalidConfigurationException {
		if (!Files.exists(Paths.get(instance.getConfigPath()))) {
			throw new InvalidConfigurationException(String.format(CONF_NOT_FOUND, instance.getConfigPath()));
		}
		try {
			FileInputStream fis = new FileInputStream(instance.getConfigPath());
			Yaml yaml = new Yaml();
			T config = yaml.loadAs(fis, clazz);
			config.setConfigPath(instance.getConfigPath());
			System.out.println("Loaded config from disk");
			return config;
		} catch (IOException | NullPointerException e) {
			throw new InvalidConfigurationException(String.format(CONF_CORRUPTED, instance.getConfigPath()));
		}
	}

	/**
	 * Prompt user to decide if advanced settings should be changed.
	 * 
	 * @return True if the user chooses to do so.
	 */
	private boolean promptUserForAdvancedSettings(Scanner s) {
		Matcher answerMatcher = null;
		Pattern answerPattern = Pattern.compile("^[yn]+|\\s*");

		boolean validAnswer = false;
		String rawAnswer = "";

		while (!validAnswer) {
			System.out.print("Change advanced settings ? [Y]/n: ");
			rawAnswer = s.nextLine().toLowerCase();

			answerMatcher = answerPattern.matcher(rawAnswer);
			validAnswer = answerMatcher.matches();
		}

		return !rawAnswer.equals("n");
	}

	private T promptUser() {
		System.out.println("Please enter the following fields. Default values are in [].");
		System.out.println("To use the default value, hit return.");
		T config = null;

		try (Scanner s = new Scanner(new CloseShieldInputStream(System.in))) {
			// Finish setting up the scanner
			s.useDelimiter(System.lineSeparator());

			// Determine if the user wants to change advanced configuration
			boolean useAdvancedSettings = promptUserForAdvancedSettings(s);

			// Load fields dynamically from the provided configuration class
			config = clazz.newInstance();
			ArrayList<Entry<Field, Prompt>> fields = getFields(useAdvancedSettings);

			for (Entry<Field, Prompt> entry : fields) {
				Field field = entry.getKey();
				String message = entry.getValue().message();
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

	private ArrayList<Entry<Field, Prompt>> getFields(boolean useAdvancedSettings) {
		ArrayList<Entry<Field, Prompt>> fields = new ArrayList<>();
		fields.addAll(getFieldsFromClass(clazz, useAdvancedSettings));
		fields.addAll(getFieldsFromClass(clazz.getSuperclass(), useAdvancedSettings));

		Collections.sort(fields, new Comparator<Entry<Field, Prompt>>() {
			@Override
			public int compare(Entry<Field, Prompt> o1, Entry<Field, Prompt> o2) {
				return o1.getValue().priority() - o2.getValue().priority();
			}
		});
		return fields;
	}

	private ArrayList<Entry<Field, Prompt>> getFieldsFromClass(Class<?> clazz, boolean useAdvancedSettings) {
		ArrayList<Entry<Field, Prompt>> fields = new ArrayList<>();

		for (Field field : clazz.getDeclaredFields()) {
			Prompt p = field.getAnnotation(Prompt.class);
			if (p != null && (!p.advanced() || (p.advanced() && useAdvancedSettings))) {
				fields.add(new SimpleEntry<>(field, p));
			}
		}
		return fields;
	}

}
