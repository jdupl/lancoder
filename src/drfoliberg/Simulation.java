package drfoliberg;

import drfoliberg.common.job.Job;
import drfoliberg.common.job.JobType;
import drfoliberg.master.Master;
import drfoliberg.worker.Worker;

public class Simulation extends Thread {

	/**
	 * This simulation runs to instantiate multiple clients and a master server under the same console. We can also
	 * access each instance and trigger events.
	 * 
	 */

	/**
	 * Attempts to encode a file locally with a single worker thread.
	 * 
	 * @param filepath
	 *            Path of the video file to encode
	 */
	public void basicSimulation(String filepath) {
		System.out.println("SIM: Starting master now");
		Master m = new Master();
		Thread masterThread = new Thread(m);
		masterThread.start();

		Job j = new Job("testname", filepath, JobType.BITRATE_2_PASS_JOB, 1000 * 60 * 5);
		System.out.println("SIM: adding a job to master's queue !");
		m.addJob(j);

		System.out.println("SIM: Creating first worker now,");
		Worker worker1 = new Worker();
		Thread w1Thread = new Thread(worker1);
		w1Thread.start();
	}

	public void shutdownTest(String filepath) {
		try {
			Master m = new Master();
			Thread masterThread = new Thread(m);
			masterThread.start();
			sleep(5000);
			Job j = new Job("testname", filepath, JobType.BITRATE_2_PASS_JOB, 1000 * 60 * 5);
			System.out.println("SIM: adding a job to master's queue !");
			m.addJob(j);
			System.out.println("SIM: Creating first worker now,");
			Worker worker1 = new Worker();
			Thread w1Thread = new Thread(worker1);
			w1Thread.start();
			sleep(10000);
			System.out.println("SIM: Closing worker ");
			worker1.shutdown();
			sleep(5000);
			System.out.println("SIM: Closing master");
			m.shutdown();
			System.out.println("SIM: Simulation done !");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run(String filepath) {
		shutdownTest(filepath);
		// basicSimulation(filepath);
		// fullSimulation(filepath);
	}
}
