package main.java.drfoliberg;

import main.java.drfoliberg.master.Master;
import main.java.drfoliberg.worker.Worker;

public class Main {

	final static String WORKER_CONFIG_PATH = "worker_config.json";
	final static String MASTER_CONFIG_PATH = "master_config.json";

	/**
	 * CLI entry point
	 * 
	 * @param args
	 *            The user's arguments
	 */
	public static void main(String[] args) {
		if (args.length < 1 || args.length > 2) {
			printHelp();
			System.exit(-1);
		}

		if (args[0].equals("--worker")) {
			String config = WORKER_CONFIG_PATH;
			if (args.length == 2) {
				config = args[1];
			}
			Worker w = new Worker(config);
			Thread wt = new Thread(w);
			wt.start();
		} else if (args[0].equals("--master")) {
			String config = MASTER_CONFIG_PATH;
			if (args.length == 2) {
				config = args[1];
			}
			Master m = new Master(config);
			Thread mt = new Thread(m);
			mt.start();
		}

	}

	public static void printHelp() {
		System.err.println("Usage: LANcoder.jar (--master | --worker) [configPath]");
		System.err.println("Use --master to run as master OR --worker to run as worker.");
		System.err.println("Add \"/path/to/config.json\" to overide default config path.");
	}

}
