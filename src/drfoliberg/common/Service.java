package drfoliberg.common;

public abstract class Service implements Runnable {
	
	protected volatile boolean close;
	
	public void stop() {
		this.close = true;
	}
}
