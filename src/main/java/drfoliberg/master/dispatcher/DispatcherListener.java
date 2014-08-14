package drfoliberg.master.dispatcher;

public interface DispatcherListener {

	public void taskRefused(DispatchItem item);

	public void taskAccepted(DispatchItem item);

}
