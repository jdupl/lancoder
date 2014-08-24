package org.lancoder.common.task.audio;

import java.util.ArrayList;

import org.lancoder.common.codecs.Codec;
import org.lancoder.common.job.RateControlType;
import org.lancoder.common.task.TaskConfig;

public class AudioTaskConfig extends TaskConfig {

	private static final long serialVersionUID = 6772908801015911315L;
	private int channels;
	private int sampleRate;

	public AudioTaskConfig(String sourceFile, RateControlType rateControlType, int rate,
			ArrayList<String> extraEncoderArgs, Codec codec, int channels, int sampleRate) {
		super(sourceFile, rateControlType, rate, 1, codec, extraEncoderArgs);
		this.channels = channels;
		this.sampleRate = sampleRate;
	}

	public Codec getCodec() {
		return codec;
	}

	public int getChannels() {
		return channels;
	}

	public int getSampleRate() {
		return sampleRate;
	}

}
