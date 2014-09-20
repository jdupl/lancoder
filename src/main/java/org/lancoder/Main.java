package org.lancoder;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;

import org.lancoder.common.Config;
import org.lancoder.common.exceptions.InvalidConfiguration;
import org.lancoder.master.Master;
import org.lancoder.master.MasterConfig;
import org.lancoder.worker.Worker;
import org.lancoder.worker.WorkerConfig;

public class Main {
	/**
	 * CLI entry point
	 * 
	 * @param args
	 *            The user's arguments
	 * @throws InvalidConfiguration
	 */
	public static void main(String[] args) throws InvalidConfiguration {
		Namespace parsed = parse(args);
		boolean isWorker = parsed.getBoolean("worker");
		boolean promptInit = parsed.getBoolean("init_prompt");
		boolean defaultInit = parsed.getBoolean("init_default");
		String config = parsed.getString("config");
		boolean mustInit = promptInit || defaultInit;

		Class<? extends Config> clazz = isWorker ? WorkerConfig.class : MasterConfig.class;
		ConfigFactory<? extends Config> factory = new ConfigFactory<>(clazz, config);
		Config conf = mustInit ? factory.init(promptInit) : factory.load();

		Runnable r = isWorker ? new Worker((WorkerConfig) conf) : new Master((MasterConfig) conf);
		new Thread(r).start();
	}

	private static Namespace parse(String[] args) {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("lancoder");
		MutuallyExclusiveGroup group = parser.addMutuallyExclusiveGroup().required(true);
		group.addArgument("--worker").action(Arguments.storeTrue());
		group.addArgument("--master").action(Arguments.storeTrue());
		MutuallyExclusiveGroup group2 = parser.addMutuallyExclusiveGroup();
		group2.addArgument("--init-prompt").action(Arguments.storeTrue());
		group2.addArgument("--init-default").action(Arguments.storeTrue());
		parser.addArgument("--config");
		return parser.parseArgsOrFail(args);
	}
}
