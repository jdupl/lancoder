package drfoliberg.worker;

public class CurrentTaskStatus {

	private boolean active;
	private long framesDone;
	private float fps;
	private long framesTotal;

	public CurrentTaskStatus(long framesTotal) {
		this.framesTotal = framesTotal;
	}

	public synchronized float getProgress() {
		float percentToComplete = ((float) framesDone / framesTotal) * 100;
		return percentToComplete;
	}

	public synchronized long getFramesDone() {
		return framesDone;
	}

	public synchronized void setFramesDone(long framesDone) {
		this.framesDone = framesDone;
	}

	public synchronized float getFps() {
		return fps;
	}

	public synchronized void setFps(float fps) {
		this.fps = fps;
	}

	public synchronized boolean isActive() {
		return active;
	}

	public synchronized void setActive(boolean active) {
		this.active = active;
	}

}
