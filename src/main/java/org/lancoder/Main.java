package org.lancoder;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.UnavailableException;

import org.lancoder.common.Container;
import org.lancoder.common.config.Config;
import org.lancoder.common.config.ConfigFactory;
import org.lancoder.common.config.ConfigManager;
import org.lancoder.common.exceptions.InvalidConfigurationException;
import org.lancoder.common.logging.LogFormatter;
import org.lancoder.master.Master;
import org.lancoder.worker.Worker;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {

	private final static String LANCODER_VERSION = "0.2.0-beta-2";

	/**
	 * CLI entry point
	 *
	 * @param args
	 *            The user's arguments
	 */
	public static void main(String[] args) {
		try {
			run(parse(args));
		} catch (InvalidConfigurationException e) {
			// Display exception message to explain cause of fatal crash to user
			Logger.getLogger("lancoder").severe(e.getMessage());
			System.exit(-1);
		}
	}

	/**
	 * Instantiates configuration and core from args.
	 *
	 * @param argsNamespace
	 *            The parsed user's args
	 * @throws InvalidConfigurationException
	 *             Any fatal exception with the configuration. User needs to fix arguments.
	 * @throws UnavailableException
	 */
	private static void run(Namespace argsNamespace) throws InvalidConfigurationException {
		LogManager manager = LogManager.getLogManager();
		manager.reset();

		Logger logger = Logger.getLogger("lancoder");
		logger.setLevel(Level.FINEST);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.FINEST);

		handler.setFormatter(new LogFormatter());
		logger.addHandler(handler);
		manager.addLogger(logger);

		logger.info(String.format("Runnning lancoder %s%n", LANCODER_VERSION));

		boolean initAndExit = argsNamespace.getBoolean("init_default");

		Class<? extends Container> clazz = argsNamespace.getBoolean("worker") ? Worker.class : Master.class;

		final Container container = getContainerInstance(clazz, argsNamespace);

		// Add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Logger logger = Logger.getLogger("lancoder");

				container.shutdown();
				logger.info("Lancoder exited cleanly !\n");
			}
		});

		// Start lancoder
		if (!initAndExit) {
			container.bootstrap();
			new Thread(container).start();
		}
	}

	/**
	 * Function to avoid bloated try-catch
	 *
	 * @param clazz
	 *            The class of the container to instantiate
	 * @param parsed
	 * @return The container
	 * @throws UnavailableException
	 */
	private static Container getContainerInstance(Class<? extends Container> clazz, Namespace argsNamespace) {
		try {
			Container container = clazz.getConstructor().newInstance();
			ConfigManager<? extends Config> manager = getConfigManager(container, argsNamespace);

			container.setConfigManager(manager);
			manager.load();

			return container;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// java pls
			e.printStackTrace();
			throw new UnsupportedClassVersionError(e.getMessage());
		}
	}

	private static ConfigManager<? extends Config> getConfigManager(Container container, Namespace argsNamespace) {
		boolean promptInit = argsNamespace.getBoolean("init_prompt");
		boolean defaultInit = argsNamespace.getBoolean("init_default");
		boolean overwrite = argsNamespace.getBoolean("overwrite");
		String configPath = argsNamespace.getString("config");
		boolean debug = argsNamespace.getBoolean("debug");

		boolean isNewConfig = promptInit || defaultInit;

		// Initialize config factory from container's config type
		ConfigFactory<? extends Config> configFactory = new ConfigFactory<>(container.getConfigClass(), configPath);

		// Initialize config manager from factory
		ConfigManager<? extends Config> manager = isNewConfig ? configFactory.init(promptInit, overwrite)
				: configFactory.getManager();
		return manager;
	}

	private static Namespace parse(String[] args) {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("lancoder")
				.defaultHelp(false)
				.version("${prog} " + LANCODER_VERSION);
		parser.addArgument("--version", "-v")
				.action(Arguments.version())
				.help("show the current version and exit");

		MutuallyExclusiveGroup group = parser.addMutuallyExclusiveGroup()
				.required(true)
				.description("Run a master or worker instance");
		group.addArgument("--worker", "-w")
				.action(Arguments.storeTrue())
				.help("run the worker");
		group.addArgument("--master", "-m")
				.action(Arguments.storeTrue())
				.help("run the master");

		MutuallyExclusiveGroup group2 = parser.addMutuallyExclusiveGroup();
		group2.addArgument("--init-prompt", "-i")
				.action(Arguments.storeTrue())
				.help("intialize configuration and prompt user");
		group2.addArgument("--init-default", "-I")
				.action(Arguments.storeTrue())
				.help("initialize default config and exit");

		parser.addArgument("--debug", "-d")
				.action(Arguments.storeTrue())
				.help("Run lancoder in debug mode. Does not delete parts files after remuxing.");
		parser.addArgument("--config", "-c")
				.help("specify the config file");
		parser.addArgument("--overwrite", "-o")
				.action(Arguments.storeTrue())
				.help("if flag is set, overwrite old config");

		return parser.parseArgsOrFail(args);
	}
}
