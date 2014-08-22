package org.lancoder.common.task.audio;

import org.lancoder.common.codecs.Codec;
import org.lancoder.common.file_components.streams.AudioStream;
import org.lancoder.common.task.Task;
import org.lancoder.common.task.video.TaskInfo;

public class AudioEncodingTask extends Task {

	private static final long serialVersionUID = 1319651638856267785L;

	private Codec codec;
	private int channels;
	private int sampleRate;

	public AudioEncodingTask(TaskInfo info, AudioStream stream, AudioTaskConfig config) {
		super(info, stream, config);
		this.codec = config.getCodec();
		this.channels = config.getChannels();
		this.sampleRate = config.getSampleRate();
	}

	@Override
	public AudioStream getStream() {
		return (AudioStream) this.stream;
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
