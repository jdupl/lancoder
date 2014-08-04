package drfoliberg.common;


public interface ServerListener {

	public void serverShutdown(Service server);

	public void serverFailure(Exception e, Service server);
}
