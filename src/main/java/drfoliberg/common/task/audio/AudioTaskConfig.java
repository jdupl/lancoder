package drfoliberg.common.task.audio;

import java.util.ArrayList;

import drfoliberg.common.codecs.Codec;
import drfoliberg.common.job.RateControlType;
import drfoliberg.common.task.TaskConfig;

public class AudioTaskConfig extends TaskConfig {

	private static final long serialVersionUID = 6772908801015911315L;

	private Codec codec;
	private int channels;
	private int sampleRate;

	public AudioTaskConfig(String sourceFile, RateControlType rateControlType, int rate,
			ArrayList<String> extraEncoderArgs, Codec codec, int channels, int sampleRate) {
		super(sourceFile, rateControlType, rate, 1, extraEncoderArgs);
		this.codec = codec;
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
