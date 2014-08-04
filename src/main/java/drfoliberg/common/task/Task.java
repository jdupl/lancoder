package drfoliberg.common.task;

import java.io.Serializable;

import drfoliberg.common.status.TaskState;

public class Task implements Serializable {

	private static final long serialVersionUID = 1570513115706156687L;
	protected TaskState taskState;

	public Task() {
		this.taskState = TaskState.TASK_TODO;
	}

	public TaskState getTaskState() {
		return taskState;
	}

	public void setTaskState(TaskState taskState) {
		this.taskState = taskState;
	}
}
