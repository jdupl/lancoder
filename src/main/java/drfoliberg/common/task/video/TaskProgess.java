package drfoliberg.common.task.video;

import java.io.Serializable;

public class TaskProgess implements Serializable {

	private static final long serialVersionUID = 7437966237627538221L;
	protected long timeStarted;
	protected long timeElapsed;
	protected long timeEstimated;
	protected long framesCompleted;
	protected double fps;
	protected int currentPass;
	/**
	 * Progress is always calculated in Task object itself. We still need this field here for serialization.
	 */
	protected float progress;

	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
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

	public int getCurrentPass() {
		return currentPass;
	}

	public void setCurrentPass(int currentPass) {
		this.currentPass = currentPass;
	}

}
