package drfoliberg.common.task;

import java.io.Serializable;

import drfoliberg.common.Status;

public class TaskStatus implements Serializable {

	private static final long serialVersionUID = 7437966237627538221L;
	protected long timeElapsed;
	protected long timeEstimated;
	protected double fps;
	protected Status status;
	protected float progress;

	public TaskStatus() {
		this.status = Status.JOB_TODO;
	}

	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	public long getTimeElapsed() {
		return timeElapsed;
	}

	public void setTimeElapsed(long timeElapsed) {
		this.timeElapsed = timeElapsed;
	}

	public long getTimeEstimated() {
		return timeEstimated;
	}

	public void setTimeEstimated(long timeEstimated) {
		this.timeEstimated = timeEstimated;
	}

	public double getFps() {
		return fps;
	}

	public void setFps(double fps) {
		this.fps = fps;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

}
