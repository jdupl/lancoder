package org.lancoder;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;

import org.lancoder.common.Container;
import org.lancoder.common.config.Config;
import org.lancoder.common.exceptions.InvalidConfigurationException;
import org.lancoder.master.Master;
import org.lancoder.master.MasterConfig;
import org.lancoder.worker.Worker;
import org.lancoder.worker.WorkerConfig;

public class Main {

	private final static String LANCODER_VERSION = "0.1.0-alpha";

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
		String config = parsed.getString("config");

		boolean mustInit = promptInit || defaultInit;
		Class<? extends Config> clazz = isWorker ? WorkerConfig.class : MasterConfig.class;
		ConfigFactory<? extends Config> factory = new ConfigFactory<>(clazz, config);
		final Config conf = mustInit ? factory.init(promptInit, overwrite) : factory.load();
		System.out.print(conf.toString());

		final Container container = isWorker ? new Worker((WorkerConfig) conf) : new Master((MasterConfig) conf);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				container.shutdown();
				System.out.println("Lancoder exited cleanly !");
			}
		});
		new Thread(container).start();
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
				.help("initialise default config (you should edit that file after afterwards)");

		parser.addArgument("--config", "-c")
				.help("specify the config file");
		parser.addArgument("--overwrite", "-o")
				.action(Arguments.storeTrue())
				.help("if flag is set, overwrite old config");
		return parser.parseArgsOrFail(args);
	}
}
