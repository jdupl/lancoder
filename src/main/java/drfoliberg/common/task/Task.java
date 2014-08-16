package drfoliberg.common.task;

import java.io.Serializable;

import drfoliberg.common.file_components.streams.Stream;
import drfoliberg.common.status.TaskState;

public class Task implements Serializable {

	private static final long serialVersionUID = 1570513115706156687L;
	protected TaskState taskState = TaskState.TASK_TODO;
	protected int taskId;
	protected String jobId;
	protected Stream stream;

	public Task(String jobId, int taskId, Stream stream) {
		this.jobId = jobId;
		this.taskId = taskId;
		this.stream = stream;
	}

	public Stream getStream() {
		return stream;
	}

	public TaskState getTaskState() {
		return taskState;
	}

	public void setTaskState(TaskState taskState) {
		this.taskState = taskState;
	}

	public void reset() {
		this.taskState = TaskState.TASK_TODO;
	}

	public String getJobId() {
		return this.jobId;
	}

	public int getTaskId() {
		return this.taskId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Task) {
			Task other = (Task) obj;
			return other.taskId == this.taskId && other.jobId.equals(this.jobId);
		}
		return false;
	}
}
