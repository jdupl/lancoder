package org.lancoder;

import org.lancoder.master.Master;
import org.lancoder.worker.Worker;

public class Main {
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
		// Get user's config path
		String configpath = args.length == 2 ? configpath = args[1] : null;
		Runnable r = null;

		if (args[0].equals("--worker")) {
			// r = new Worker(configpath);
		} else if (args[0].equals("--master")) {
			if (args.length == 2) {
				configpath = args[1];
			}
			// r = new Master(configpath);
		} else {
			printHelp();
			System.exit(-1);
		}
		Thread t = new Thread(r);
		t.start();
	}

	public static void printHelp() {
		System.err.println("Usage: LANcoder.jar (--master | --worker) [--init] [--config] [configPath]");
		System.err.println("Use --master to run as master OR --worker to run as worker.");
		System.err.println("Use --init to generate a new config with user input.");
		System.err.println("Use --config \"/path/to/config.json\" to overide default config path.");
	}

}
