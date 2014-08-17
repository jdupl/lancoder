package drfoliberg.common.task;

import java.io.Serializable;

import drfoliberg.common.file_components.streams.Stream;
import drfoliberg.common.file_components.streams.VideoStream;
import drfoliberg.common.job.JobConfig;
import drfoliberg.common.progress.TaskProgress;
import drfoliberg.common.status.TaskState;
import drfoliberg.common.task.video.TaskInfo;

public class Task implements Serializable {

	private static final long serialVersionUID = 1570513115706156687L;

	protected Stream stream;
	protected TaskInfo taskInfo;
	protected TaskProgress taskProgress;
	protected JobConfig jobConfig;

	public Task(JobConfig config, TaskInfo taskInfo, VideoStream stream) {
		this.jobConfig = config;
		this.stream = stream;
		taskProgress = new TaskProgress(taskInfo.getEstimatedFramesCount(), config.getPasses());
	}

	public TaskState getTaskState() {
		return taskProgress.getTaskState();
	}

	public void setTaskState(TaskState taskState) {
		taskProgress.setTaskState(taskState);
	}

	public void reset() {
		taskProgress.reset();
	}

	public Stream getStream() {
		return stream;
	}

	public TaskInfo getTaskInfo() {
		return this.taskInfo;
	}

	public TaskProgress getTaskProgress() {
		return taskProgress;
	}

	public JobConfig getJobConfig() {
		return jobConfig;
	}

	public int getTaskId() {
		return taskInfo.getTaskId();
	}

	public String getJobId() {
		return taskInfo.getJobId();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Task) {
			Task other = (Task) obj;
			return other.getTaskInfo().getTaskId() == this.getTaskInfo().getTaskId()
					&& other.getTaskInfo().getJobId().equals(this.getTaskInfo().getJobId());
		}
		return false;
	}
}
