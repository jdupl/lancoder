package drfoliberg.common.task;

public class EncodingTask extends Task {

	private static final long serialVersionUID = -3848847899645447887L;

	private long startTime;
	private long endTime;
	private Pass passes;

	public EncodingTask(int taskId, String fileLocation) {
		super(taskId, fileLocation);
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long ms) {
		this.startTime = ms;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

}
