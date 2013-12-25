package drfoliberg.worker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import drfoliberg.common.task.Task;

public class Work extends Thread {

	private InetAddress masterIp;
	private Task task;
	private Worker callback;

	public Work(Worker w, Task t, InetAddress masterIp) {
		this.masterIp = masterIp;
		task = t;
		callback = w;
	}

	@Override
	public void run() {
		try {
			System.out.println("WORKER WORK THREAD: Executing a task!");
			sleep(10000);
			System.out.println("WORKER WORK THREAD: Done! Calling back the worker");
			callback.taskDone(task, masterIp);
			System.out.println("WORKER WORK THREAD: callback called");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
