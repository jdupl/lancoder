package org.lancoder.common.task.video;

import java.io.Serializable;
import java.util.ArrayList;

import org.lancoder.common.job.FFmpegPreset;
import org.lancoder.common.job.RateControlType;
import org.lancoder.common.task.TaskConfig;

public class VideoTaskConfig extends TaskConfig implements Serializable {

	private static final long serialVersionUID = -8201664961243820323L;

	protected FFmpegPreset preset;

	public VideoTaskConfig(String sourceFile, RateControlType rateControlType, int rate, int passes,
			ArrayList<String> extraEncoderArgs, FFmpegPreset preset) {
		super(sourceFile, rateControlType, rate, passes, extraEncoderArgs);
		this.preset = preset;
	}

	public FFmpegPreset getPreset() {
		return preset;
	}

	public void setPreset(FFmpegPreset preset) {
		this.preset = preset;
	}

}