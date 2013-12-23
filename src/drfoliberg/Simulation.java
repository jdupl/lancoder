package drfoliberg;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import drfoliberg.master.Master;
import drfoliberg.master.Node;
import drfoliberg.task.Task;
import drfoliberg.worker.Worker;

public class Simulation extends Thread {

	/**
	 * This simulation runs so we have a client-server in the same console.
	 * 
	 */
	public Simulation() throws IOException {

	}

	public void run() {

		InetAddress masterIp;
		try {
			masterIp = InetAddress.getByName("127.0.0.1");

//			System.out.println("SIM: Starting master now");
//			Master m = new Master();
			System.out.println("SIM: Creating first worker now,");
			Worker worker1 = new Worker("worker1", masterIp, 1337, 1338);
			worker1.start();			
			System.out.println("SIM: Faking that master is not up... waiting 5 seconds to start master.");
			sleep(5000);
			System.out.println("SIM: Starting master now");
			Master m = new Master();


			// m.start();
			// System.out.println("SIM: Forcing master to disconnect his worker in 2 seconds");
			// sleep(2000);
			// System.out.println("SIM: disconnecting worker now");
			// m.disconnectNode(new Node(masterIp));
			//
			sleep(2000);
			System.out.println("SIM: sending a task to the worker!");
			//TODO
			m.dispatch(new Task(), new Node(masterIp, 1338));
			System.out.println("SIM: simulation completed !");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
