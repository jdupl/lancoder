package drfoliberg.master;

import drfoliberg.common.Node;
import drfoliberg.common.task.Task;

public interface DispatcherListener {

	public void taskRefused(Task t, Node n);

	public void taskAccepted(Task t, Node n);

}
