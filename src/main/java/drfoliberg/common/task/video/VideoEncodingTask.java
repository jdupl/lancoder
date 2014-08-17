package drfoliberg.common.task.video;

import java.io.Serializable;
import java.util.ArrayList;

import drfoliberg.common.file_components.streams.VideoStream;
import drfoliberg.common.job.FFmpegPreset;
import drfoliberg.common.job.JobConfig;
import drfoliberg.common.job.RateControlType;
import drfoliberg.common.progress.TaskProgress;
import drfoliberg.common.status.TaskState;
import drfoliberg.common.task.Task;

public class VideoEncodingTask extends Task implements Serializable {

	private static final long serialVersionUID = -8705492902098705162L;
	protected TaskInfo taskInfo;
	protected TaskProgress taskProgress;
	protected JobConfig jobConfig;

	public VideoEncodingTask(JobConfig config, TaskInfo taskInfo, VideoStream stream) {
		super(config, taskInfo, stream);
		this.jobConfig = config;
		taskProgress = new TaskProgress(taskInfo.estimatedFramesCount, config.getPasses());
	}

	public ArrayList<String> getRateControlArgs() {
		ArrayList<String> args = new ArrayList<>();
		switch (jobConfig.getRateControlType()) {
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
		if (jobConfig.getPreset() != null) {
			args.add("-preset");
			args.add(jobConfig.getPreset().toString());
		}
		return args;
	}

	public void reset() {
		taskProgress.setFramesCompleted(0);
		taskProgress.setTaskState(TaskState.TASK_TODO);
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
        return ((float) taskProgress.getFramesCompleted() / taskInfo.getEstimatedFramesCount()) * 100;
	}

	public int getPasses() {
		return jobConfig.getPasses();
	}

	public RateControlType getRateControlType() {
		return jobConfig.getRateControlType();
	}


	public int getRate() {
		return jobConfig.getRate();
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
		return jobConfig.getSourceFile();
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
		return jobConfig.getExtraEncoderArgs();
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
		return jobConfig.getPreset();
	}

	public TaskState getTaskState() {
		// TODO Auto-generated method stub
		return null;
	}

}
