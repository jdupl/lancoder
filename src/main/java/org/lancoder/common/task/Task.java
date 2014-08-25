package org.lancoder.common.task;

import java.io.Serializable;
import java.util.ArrayList;

import org.lancoder.common.codecs.Codec;
import org.lancoder.common.file_components.streams.Stream;
import org.lancoder.common.job.RateControlType;
import org.lancoder.common.progress.Progress;
import org.lancoder.common.progress.TaskProgress;
import org.lancoder.common.progress.Unit;
import org.lancoder.common.status.TaskState;
import org.lancoder.common.task.video.TaskInfo;

public abstract class Task implements Serializable {

	private static final long serialVersionUID = 1570513115706156687L;

	protected Stream stream;
	protected TaskInfo taskInfo;
	protected TaskProgress taskProgress;

	protected String sourceFile;
	protected RateControlType rateControlType;
	protected int rate; // kbps or crf TODO use BiterateControl ?
	protected int passes;
	protected Codec codec;
	protected ArrayList<String> extraEncoderArgs; // TODO usage this to allow --slow-first-pass and other overrides

	public Task(TaskInfo taskInfo, Stream stream, TaskConfig config, Unit unit) {
		this.stream = stream;
		this.taskInfo = taskInfo;
		this.sourceFile = stream.getRelativeFile();
		this.rateControlType = config.getRateControlType();
		this.rate = config.getRate();
		this.passes = config.getPasses();
		this.codec = config.getCodec();
		this.extraEncoderArgs = config.getExtraEncoderArgs();
		taskProgress = new TaskProgress(taskInfo.getEstimatedFramesCount(), passes, unit);
	}

	public Codec getCodec() {
		return codec;
	}

	public void setTaskProgress(TaskProgress taskProgress) {
		this.taskProgress = taskProgress;
	}

	public Progress getCurrentStep() {
		return taskProgress.getCurrentStep();
	}

	public void start() {
		taskProgress.start();
	}

	public void update(long units) {
		taskProgress.update(units);
	}

	public void update(long units, double speed) {
		taskProgress.update(units, speed);
	}

	public void completeStep() {
		taskProgress.completeStep();
	}

	public void complete() {
		taskProgress.complete();
	}

	public int getCurrentPassIndex() {
		return taskProgress.getCurrentPassIndex();
	}

	public TaskState getTaskState() {
		return taskProgress.getTaskState();
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

	public TaskProgress getTaskStatus() {
		return taskProgress;
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

	public ArrayList<String> getExtraEncoderArgs() {
		return extraEncoderArgs;
	}

	public int getCurrentPass() {
		return taskProgress.getCurrentPassIndex();
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
