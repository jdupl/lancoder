package drfoliberg.common.task;

import java.io.Serializable;

import drfoliberg.common.Status;

public class TaskStatus implements Serializable {

	private static final long serialVersionUID = 7437966237627538221L;
	protected long timeStarted;
	protected long timeElapsed;
	protected long timeEstimated;
	protected long framesCompleted;
	protected double fps;
	protected Status status;

	public TaskStatus() {
		this.status = Status.JOB_TODO;
	}

	public long getTimeStarted() {
		return timeStarted;
	}

	public void setTimeStarted(long timeStarted) {
		this.timeStarted = timeStarted;
	}

	public long getFramesCompleted() {
		return framesCompleted;
	}

	public void setFramesCompleted(long framesCompleted) {
		this.framesCompleted = framesCompleted;
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
