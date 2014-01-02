package drfoliberg.worker;

import java.net.InetAddress;

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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
