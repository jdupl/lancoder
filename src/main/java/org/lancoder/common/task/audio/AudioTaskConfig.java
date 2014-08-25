package org.lancoder.common.task.audio;

import java.util.ArrayList;

import org.lancoder.common.codecs.ChannelDisposition;
import org.lancoder.common.codecs.Codec;
import org.lancoder.common.job.RateControlType;
import org.lancoder.common.task.TaskConfig;

public class AudioTaskConfig extends TaskConfig {

	private static final long serialVersionUID = 6772908801015911315L;
	private ChannelDisposition channels;
	private int sampleRate;

	public AudioTaskConfig(String sourceFile, RateControlType rateControlType, int rate,
			ArrayList<String> extraEncoderArgs, Codec codec, ChannelDisposition channels, int sampleRate) {
		super(sourceFile, rateControlType, rate, 1, codec, extraEncoderArgs);
		this.channels = channels;
		this.sampleRate = sampleRate;
	}

	public Codec getCodec() {
		return codec;
	}

	public ChannelDisposition getChannels() {
		return channels;
	}

	public int getSampleRate() {
		return sampleRate;
	}

}
