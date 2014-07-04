package main.java.drfoliberg.common;

public abstract class Service implements Runnable {

	protected volatile boolean close;

	public void stop() {
		this.close = true;
	}

	public void serviceFailure(Exception e) {
		e.printStackTrace();
		// TODO send to service listener
	}
}