package drfoliberg.common;


public interface ServerListener {

	public void serverShutdown(RunnableService server);

	public void serverFailure(Exception e, RunnableService server);
}
