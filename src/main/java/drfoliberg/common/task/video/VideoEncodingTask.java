package drfoliberg.common.task.video;

import java.io.Serializable;
import java.util.ArrayList;

import drfoliberg.common.file_components.streams.VideoStream;
import drfoliberg.common.job.FFmpegPreset;
import drfoliberg.common.progress.TaskProgress;
import drfoliberg.common.status.TaskState;
import drfoliberg.common.task.Task;

public class VideoEncodingTask extends Task implements Serializable {

	private static final long serialVersionUID = -8705492902098705162L;
	
	protected FFmpegPreset preset;

	public VideoEncodingTask(TaskInfo taskInfo, VideoStream stream, VideoTaskConfig config) {
		super(taskInfo, stream,config);
		this.preset = config.getPreset();
		taskProgress = new TaskProgress(taskInfo.estimatedFramesCount, passes);
	}

	public ArrayList<String> getRateControlArgs() {
		ArrayList<String> args = new ArrayList<>();
		switch (this.rateControlType) {
		case VBR:
			args.add("-b:v");
			args.add(String.format("%dk", this.rate));
			break;
		case CRF:
			args.add("-crf");
			args.add(String.format("%d", this.rate));
			break;
		default:
			// TODO throw exception
			break;
		}
		return args;
	}

	public ArrayList<String> getPresetArg() {
		ArrayList<String> args = new ArrayList<>();
		if (getPreset() != null) {
			args.add("-preset");
			args.add(getPreset().toString());
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

	public FFmpegPreset getPreset() {
		return this.preset;
	}

	public TaskState getTaskState() {
		return this.taskProgress.getTaskState();
	}

}
