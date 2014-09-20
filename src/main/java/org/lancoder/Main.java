package org.lancoder;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;

import org.lancoder.common.Config;
import org.lancoder.common.exceptions.MissingConfiguration;
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
	 * @throws MissingConfiguration
	 */
	public static void main(String[] args) throws MissingConfiguration {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("lancoder");
		MutuallyExclusiveGroup group = parser.addMutuallyExclusiveGroup().required(true);
		group.addArgument("--worker").action(Arguments.storeTrue());
		group.addArgument("--master").action(Arguments.storeTrue());
		MutuallyExclusiveGroup group2 = parser.addMutuallyExclusiveGroup();
		group2.addArgument("--init-prompt").action(Arguments.storeTrue());
		group2.addArgument("--init-default").action(Arguments.storeTrue());

		Namespace parsed = parser.parseArgsOrFail(args);

		Runnable r = null;
		boolean isWorker = parsed.getBoolean("worker");
		Class clazz = isWorker ? WorkerConfig.class : MasterConfig.class;
		if (isWorker) {
			clazz = WorkerConfig.class;
		}
		ConfigFactory<Config> factory = new ConfigFactory<>(clazz);
		Config conf = factory.load();
		if (isWorker) {
			r = new Worker((WorkerConfig) conf);
		} else {
			r = new Master((MasterConfig) conf);
		}
		Thread t = new Thread(r);
		t.start();
	}
}
