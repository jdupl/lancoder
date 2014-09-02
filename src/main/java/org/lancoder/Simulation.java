package org.lancoder;

import org.lancoder.master.Master;
import org.lancoder.worker.Worker;

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
		System.out.println("SIM: Starting master now");
		Master m = new Master();
		Thread masterThread = new Thread(m);
		masterThread.start();

		System.out.println("SIM: Creating first worker now,");
		Worker worker1 = new Worker();
		Thread w1Thread = new Thread(worker1);
		w1Thread.start();

	}

	public void run() {
		basicSimulation();
	}
}
