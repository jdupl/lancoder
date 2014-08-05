package drfoliberg.common.task.video;

import java.io.Serializable;
import java.util.ArrayList;

import drfoliberg.common.job.FFmpegPreset;
import drfoliberg.common.job.JobConfig;
import drfoliberg.common.job.RateControlType;
import drfoliberg.common.status.TaskState;
import drfoliberg.common.task.Task;

public class VideoEncodingTask extends Task implements Serializable {

	private static final long serialVersionUID = -8705492902098705162L;
	protected TaskInfo taskInfo;
	protected TaskProgess taskProgress;

	public VideoEncodingTask(int taskId, String jobId, JobConfig config) {
		super(jobId, taskId);
		taskInfo = new TaskInfo(config);
		taskInfo.setTaskId(taskId);
		taskInfo.setJobId(jobId);
		taskProgress = new TaskProgess();
	}

	public ArrayList<String> getRateControlArgs() {
		ArrayList<String> args = new ArrayList<>();
		switch (taskInfo.getRateControlType()) {
		case VBR:
			args.add("-b:v");
			args.add(String.format("%dk", this.getRate()));
			break;
		case CRF:
			args.add("-crf");
			args.add(String.format("%d", this.getRate()));
			break;
		default:
			// TODO throw exception
			break;
		}
		return args;
	}

	public ArrayList<String> getPresetArg() {
		ArrayList<String> args = new ArrayList<>();
		if (taskInfo.getPreset() != null) {
			args.add("-preset");
			args.add(taskInfo.getPreset().toString());
		}
		return args;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof VideoEncodingTask)) {
			return false;
		}
		VideoEncodingTask other = (VideoEncodingTask) obj;
		if (other.getJobId() == null || this.getJobId() == null) {
			return false;
		}
		return other.getTaskId() == this.getTaskId() && other.getJobId().equals(this.getJobId());
	}

	public void reset() {
		taskProgress.setFramesCompleted(0);
		setTaskState(TaskState.TASK_TODO);
	}

	public void start() {
		setTimeStarted(System.currentTimeMillis());
		setTaskState(TaskState.TASK_COMPUTING);
	}

	public long getETA() {
		long elapsedMs = System.currentTimeMillis() - getTimeStarted();
		return (long) (elapsedMs / (getProgress() / 100));
	}

	public float getProgress() {
		float percentToComplete = ((float) taskProgress.getFramesCompleted() / taskInfo.getEstimatedFramesCount()) * 100;
		return percentToComplete;
	}

	public byte getPasses() {
		return taskInfo.getPasses();
	}

	public void setPasses(byte passes) {
		taskInfo.setPasses(passes);
	}

	public RateControlType getRateControlType() {
		return taskInfo.getRateControlType();
	}

	public void setRateControlType(RateControlType rateControlType) {
		taskInfo.setRateControlType(rateControlType);
	}

	public int getRate() {
		return taskInfo.getRate();
	}

	public void setRate(int rate) {
		taskInfo.setRate(rate);
	}

	public long getFramesCompleted() {
		return taskProgress.getFramesCompleted();
	}

	public void setFramesCompleted(long framesCompleted) {
		taskProgress.setFramesCompleted(framesCompleted);
	}

	public void setTaskStatus(TaskProgess taskStatus) {
		this.taskProgress = taskStatus;
	}

	public TaskProgess getTaskStatus() {
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
		return taskInfo.getSourceFile();
	}

	public void setSourceFile(String sourceFile) {
		taskInfo.setSourceFile(sourceFile);
	}

	public int getTaskId() {
		return taskInfo.getTaskId();
	}

	public void setTaskId(int taskId) {
		taskInfo.setTaskId(taskId);
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

	public void setPreset(FFmpegPreset preset) {
		taskInfo.setPreset(preset);
	}

	public ArrayList<String> getExtraEncoderArgs() {
		return taskInfo.getExtraEncoderArgs();
	}

	public void setExtraEncoderArgs(ArrayList<String> extraEncoderArgs) {
		taskInfo.setExtraEncoderArgs(extraEncoderArgs);
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

	public FFmpegPreset getPreset() {
		return taskInfo.getPreset();
	}

}
