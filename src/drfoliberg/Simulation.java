package drfoliberg;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import drfoliberg.common.task.Job;
import drfoliberg.common.task.JobType;
import drfoliberg.master.Master;
import drfoliberg.worker.Worker;

public class Simulation extends Thread {

	/**
	 * This simulation runs so we have a client-server in the same console.
	 * 
	 */
	public Simulation() throws IOException {

	}
	
	public void basicSimulation() {
		InetAddress masterIp;
		try {
			masterIp = InetAddress.getByName("127.0.0.1");
			System.out.println("SIM: Creating first worker now,");
			Worker worker1 = new Worker("worker1", masterIp, 1337, 1338);
			Thread w1Thread = new Thread(worker1);
			w1Thread.start();
			System.out
					.println("SIM: Faking that master is not up... waiting 5 seconds to start master.");
			sleep(5000);
			System.out.println("SIM: Starting master now");
			
			Master m = new Master();
			Thread masterThread = new Thread(m);
			masterThread.start();
			
			sleep(5000);
			Job j = new Job("testname", "My.Movie.mkv", JobType.BITRATE_2_PASS_JOB, 5 * 60 * 1000, 120 * 60 * 1000 + 11548);
			System.out.println("SIM: adding a job to master's queue !");
			m.addJob(j);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void fullSimulation(){
		InetAddress masterIp;
		try {
			masterIp = InetAddress.getByName("127.0.0.1");
			System.out.println("SIM: Creating a job");
			Job j = new Job("testname", "My.Movie.mkv", JobType.BITRATE_2_PASS_JOB, 5 * 60 * 1000, 120 * 60 * 1000 + 11548);
			System.out.println("SIM: Creating first worker now,");
			Worker worker1 = new Worker("worker1", masterIp, 1337, 1338);
			Thread w1Thread = new Thread(worker1);
			w1Thread.start();
			System.out
					.println("SIM: Faking that master is not up... waiting 5 seconds to start master.");
			sleep(5000);
			System.out.println("SIM: Starting master now");
			
			Master m = new Master();
			Thread masterThread = new Thread(m);
			masterThread.start();
			sleep(2000);
			System.out.println("SIM: adding a job to master's queue !");
			m.addJob(j);
			sleep(5000);
			System.out.println("SIM: Creating second worker now");
			Worker worker2 = new Worker("worker2", masterIp, 1337, 1339);
			Thread w2Thread = new Thread(worker2);
			w2Thread.start();
			sleep(1000);
			System.out.println("SIM: Creating third worker now");
			Worker worker3 = new Worker("worker3", masterIp, 1337, 1340);
			Thread w3Thread = new Thread(worker3);
			w3Thread.start();
			sleep(10000);
			System.out.println("SIM: shutting down worker 3");
			worker3.shutdown();
			System.out.println("SIM: simulation completed !");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
//		basicSimulation();
		fullSimulation();
	}
}
