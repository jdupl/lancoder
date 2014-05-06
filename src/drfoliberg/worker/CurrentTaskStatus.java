package drfoliberg.worker;

public class CurrentTaskStatus {

	private boolean active;
	private long startedOn;
	private long framesDone;
	private float fps;
	private long framesTotal;

	public CurrentTaskStatus(long framesTotal) {
		this.framesTotal = framesTotal;
	}

	public float getProgress() {
		float percentToComplete = ((float) framesDone / framesTotal) * 100;
		return percentToComplete;
	}

	public long getETA() {
		long elapsedMs = System.currentTimeMillis() - startedOn;
		return (long) (elapsedMs / (getProgress() / 100));
	}

	public long getStartedOn() {
		return startedOn;
	}

	public void setStartedOn(long startedOn) {
		this.startedOn = startedOn;
	}

	public long getFramesDone() {
		return framesDone;
	}

	public void setFramesDone(long framesDone) {
		this.framesDone = framesDone;
	}

	public float getFps() {
		return fps;
	}

	public void setFps(float fps) {
		this.fps = fps;
	}

	public synchronized boolean isActive() {
		return active;
	}

	public synchronized void setActive(boolean active) {
		this.active = active;
	}

}
