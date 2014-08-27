package org.lancoder.common.task.video;

import java.io.Serializable;
import java.util.ArrayList;

import org.lancoder.common.file_components.streams.VideoStream;
import org.lancoder.common.job.FFmpegPreset;
import org.lancoder.common.progress.Unit;
import org.lancoder.common.task.Task;

@Deprecated
public class VideoEncodingTask extends Task implements Serializable {

	private static final long serialVersionUID = -8705492902098705162L;
	private static final Unit DEFAULT_UNIT = Unit.FRAMES;

	protected FFmpegPreset preset;

	public VideoEncodingTask(TaskInfo taskInfo, VideoStream stream, VideoTaskConfig config) {
		super(taskInfo, stream, config, DEFAULT_UNIT);
		this.preset = config.getPreset();
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

	public FFmpegPreset getPreset() {
		return this.preset;
	}
}
