package org.lancoder;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;

import org.lancoder.common.Container;
import org.lancoder.common.config.Config;
import org.lancoder.common.config.ConfigFactory;
import org.lancoder.common.config.ConfigManager;
import org.lancoder.common.exceptions.InvalidConfigurationException;
import org.lancoder.master.Master;
import org.lancoder.worker.Worker;

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
			System.out.printf("Runnning lancoder %s%n", LANCODER_VERSION);
			run(parse(args));
		} catch (InvalidConfigurationException e) {
			// Display exception message to explain cause of fatal crash to user
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	/**
	 * Instantiates configuration and core from args.
	 * 
	 * @param parsed
	 *            The parsed user's args
	 * @throws InvalidConfigurationException
	 *             Any fatal exception with the configuration. User needs to fix arguments.
	 */
	private static void run(Namespace parsed) throws InvalidConfigurationException {
		boolean isWorker = parsed.getBoolean("worker");
		boolean promptInit = parsed.getBoolean("init_prompt");
		boolean defaultInit = parsed.getBoolean("init_default");
		boolean overwrite = parsed.getBoolean("overwrite");
		String configPath = parsed.getString("config");
		boolean debug = parsed.getBoolean("debug");

		boolean newConfig = promptInit || defaultInit;
		Class<? extends Container> clazz = isWorker ? Worker.class : Master.class;

		try {
			final Container container = clazz.getConstructor().newInstance();

			// Initialize config factory from container's config type
			ConfigFactory<? extends Config> configFactory = new ConfigFactory<>(container.getConfigClass(), configPath);

			// Initialize config manager from factory
			ConfigManager<? extends Config> manager = newConfig ? configFactory.init(promptInit, overwrite)
					: configFactory.getManager();

			manager.load();
			container.setConfigManager(manager);

			System.out.print(manager.getConfig().toString());

			// Add shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					container.shutdown();
					System.out.println("Lancoder exited cleanly !");
				}
			});

			// Start lancoder
			if (!defaultInit) {
				new Thread(container).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
