package drfoliberg.common.task;

import java.io.Serializable;
import java.util.ArrayList;

import drfoliberg.common.file_components.streams.Stream;
import drfoliberg.common.job.RateControlType;
import drfoliberg.common.progress.TaskProgress;
import drfoliberg.common.status.TaskState;
import drfoliberg.common.task.video.TaskInfo;

public abstract class Task implements Serializable {

	private static final long serialVersionUID = 1570513115706156687L;

	protected Stream stream;
	protected TaskInfo taskInfo;
	protected TaskProgress taskProgress;

	protected String sourceFile;
	protected RateControlType rateControlType;
	protected int rate; // kbps or crf TODO use BiterateControl ?
	protected int passes;
	protected ArrayList<String> extraEncoderArgs; // TODO usage this to allow --slow-first-pass and other overrides

	public Task(TaskInfo taskInfo, Stream stream, TaskConfig config) {
		this.stream = stream;
		this.taskInfo = taskInfo;

		this.sourceFile = config.getSourceFile();
		this.rateControlType = config.getRateControlType();
		this.rate = config.getRate();
		this.passes = config.getPasses();
		this.extraEncoderArgs = config.getExtraEncoderArgs();
		taskProgress = new TaskProgress(taskInfo.getEstimatedFramesCount(), passes);
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

	public int getTaskId() {
		return taskInfo.getTaskId();
	}

	public long getFramesCompleted() {
		return taskProgress.getFramesCompleted();
	}

	public void setFramesCompleted(long framesCompleted) {
		taskProgress.setFramesCompleted(framesCompleted);
	}

	public void setTaskStatus(TaskProgress taskStatus) {
		this.taskProgress = taskStatus;
	}

	public TaskProgress getTaskStatus() {
		return taskProgress;
	}

	public long getTimeElapsed() {
		return taskProgress.getTimeElapsed();
	}

	public void setTimeElapsed(long timeElapsed) {
		taskProgress.setTimeElapsed(timeElapsed);
	}

	public long getTimeEstimated() {
		return taskProgress.getTimeEstimated();
	}

	public void setTimeEstimated(long timeEstimated) {
		taskProgress.setTimeEstimated(timeEstimated);
	}

	public double getFps() {
		return taskProgress.getFps();
	}

	public void setFps(double fps) {
		taskProgress.setFps(fps);
	}

	public String getSourceFile() {
		return sourceFile;
	}

	public String getJobId() {
		return taskInfo.getJobId();
	}

	public void setJobId(String jobId) {
		taskInfo.setJobId(jobId);
	}

	public long getEncodingStartTime() {
		return taskInfo.getEncodingStartTime();
	}

	public void setEncodingStartTime(long encodingStartTime) {
		taskInfo.setEncodingStartTime(encodingStartTime);
	}

	public long getEncodingEndTime() {
		return taskInfo.getEncodingEndTime();
	}

	public void setEncodingEndTime(long encodingEndTime) {
		taskInfo.setEncodingEndTime(encodingEndTime);
	}

	public long getEstimatedFramesCount() {
		return taskInfo.getEstimatedFramesCount();
	}

	public void setEstimatedFramesCount(long estimatedFramesCount) {
		taskInfo.setEstimatedFramesCount(estimatedFramesCount);
	}

	public long getTimeStarted() {
		return taskProgress.getTimeStarted();
	}

	public void setProgress(float progress) {
		taskProgress.setProgress(progress);
	}

	public void setTimeStarted(long timeStarted) {
		taskProgress.setTimeStarted(timeStarted);
	}

	public ArrayList<String> getExtraEncoderArgs() {
		return extraEncoderArgs;
	}

	public int getCurrentPass() {
		return taskProgress.getCurrentPass();
	}

	public void setCurrentPass(int currentPass) {
		taskProgress.setCurrentPass(currentPass);
	}

	public String getOutputFile() {
		return taskInfo.getOutputFile();
	}

	public void setOutputFile(String outputFile) {
		taskInfo.setOutputFile(outputFile);
	}

	public RateControlType getRateControlType() {
		return rateControlType;
	}

	public int getRate() {
		return rate;
	}

	public int getPasses() {
		return passes;
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
