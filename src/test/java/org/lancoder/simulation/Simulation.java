package org.lancoder.simulation;
			import org.lancoder.ConfigFactory;
import org.lancoder.common.exceptions.InvalidConfigurationException;
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
	 * @throws InvalidConfigurationException
	 * 
	 */
	public void basicSimulation() throws InvalidConfigurationException {
		MasterConfig masterConfig = null;
		WorkerConfig workerConfig = null;
		ConfigFactory<MasterConfig> masterFactory = new ConfigFactory<>(MasterConfig.class);
		ConfigFactory<WorkerConfig> workerFactory = new ConfigFactory<>(WorkerConfig.class);
		try {
             masterConfig = masterFactory.load();
		} catch (InvalidConfigurationException e) {
     //  			masterConfig = masterFactory.init(true, true);
         }
		try {
			workerConfig = workerFactory.load();
		} catch (InvalidConfigurationException e) {
					workerConfig = workerFactory.init(true, true);
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
		try {
			basicSimulation();
		} catch (InvalidConfigurationException e) {
       e.printStackTrace();
 //		}
	}
}
