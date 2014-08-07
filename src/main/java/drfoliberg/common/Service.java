package drfoliberg.common;

public class Service {

	protected volatile boolean close;

	public void stop() {
		this.close = true;
	}
}