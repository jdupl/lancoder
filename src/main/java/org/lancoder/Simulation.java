package org.lancoder;

import org.lancoder.common.exceptions.MissingConfiguration;
import org.lancoder.master.Master;
import org.lancoder.master.MasterConfig;
import org.lancoder.worker.Worker;
import org.lancoder.worker.WorkerConfig;

public class Simulation extends Thread {

	/**
	 * This simulation runs to instantiate multiple clients and a master server under the same console. We can also
	 * access each instance and trigger events.
	 * 
	 */

	/**
	 * Initialize only a master and a worker. Jobs must be added via the api.
	 * 
	 */
	public void basicSimulation() {
		MasterConfig masterConfig = null;
		WorkerConfig workerConfig = null;
		ConfigFactory<MasterConfig> masterFactory = new ConfigFactory<>(MasterConfig.class);
		ConfigFactory<WorkerConfig> workerFactory = new ConfigFactory<>(WorkerConfig.class);
		try {
			masterConfig = masterFactory.load();
		} catch (MissingConfiguration e) {
			masterConfig = masterFactory.init(true);
		}
		try {
			workerConfig = workerFactory.load();
		} catch (MissingConfiguration e) {
			workerConfig = workerFactory.init(true);
		}
		System.out.println("SIM: Starting master now");
		Master m = new Master(masterConfig);
		Thread masterThread = new Thread(m);
		masterThread.start();

		System.out.println("SIM: Creating first worker now,");
		Worker worker1 = new Worker(workerConfig);
		Thread w1Thread = new Thread(worker1);
		w1Thread.start();
	}

	public void run() {
		basicSimulation();
	}
}
