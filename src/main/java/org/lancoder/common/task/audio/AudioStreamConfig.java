package org.lancoder.common.task.audio;

import java.util.ArrayList;

import org.lancoder.common.file_components.streams.AudioStream;
import org.lancoder.common.file_components.streams.original.OriginalAudioStream;
import org.lancoder.common.task.StreamConfig;
    public class AudioStreamConfig extends StreamConfig {

	private static final long serialVersionUID = 7191622871794153963L;

	public AudioStreamConfig(String jobId, ArrayList<String> extraEncoderArgs, OriginalAudioStream orignalStream,
			AudioStream destinationStream) {
		super(jobId, extraEncoderArgs, 1, orignalStream, destinationStream);
	}

	@Override
	public OriginalAudioStream getOrignalStream() {
		return (OriginalAudioStream) this.orignalStream;
	}

	@Override
	public AudioStream getOutStream() {
		return (AudioStream) this.destinationStream;
	}

}
