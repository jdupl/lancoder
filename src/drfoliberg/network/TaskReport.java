package drfoliberg.network;

public class TaskReport extends Message{

	private static final long serialVersionUID = -2146895423858055901L;

	private double progress;
	private String taskId;
	private int taskPiece;
	private long timeElapsed;
	private long timeEstimated;
	private double fps;
	
	public TaskReport() {
		super(ClusterProtocol.TASK_REPORT);
	}
	
}
