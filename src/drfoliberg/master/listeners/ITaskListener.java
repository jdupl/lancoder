package drfoliberg.master.listeners;

import drfoliberg.common.task.Task;

public interface ITaskListener {
	
	void taskFinished(Task t);
	void taskCancelled(Task t);
	
}
