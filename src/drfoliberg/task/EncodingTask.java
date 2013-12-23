package drfoliberg.task;

public class EncodingTask extends Task {

	private static final long serialVersionUID = -3848847899645447887L;

	private String encoder;
	private double fps;
	private String startTime;
	private String endTime;
	private Pass passes;
	
	
	public String getEncoder() {
		return encoder;
	}

	public void setEncoder(String encoder) {
		this.encoder = encoder;
	}

	public double getFps() {
		return fps;
	}

	public void setFps(double fps) {
		this.fps = fps;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

}
