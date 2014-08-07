package drfoliberg.common;

public abstract class Service {

	protected volatile boolean close;

	public void stop() {
		this.close = true;
	}
}