package org.lancoder.common.task.audio;

import java.util.ArrayList;

import org.lancoder.common.file_components.streams.AudioStream;
import org.lancoder.common.task.StreamConfig;

public class AudioStreamConfig extends StreamConfig {

	private static final long serialVersionUID = 7191622871794153963L;

	public AudioStreamConfig(String jobId, ArrayList<String> extraEncoderArgs, int passes, AudioStream orignalStream,
			AudioStream destinationStream) {
		super(jobId, extraEncoderArgs, passes, orignalStream, destinationStream);
	}

	@Override
	public AudioStream getOrignalStream() {
		// TODO Auto-generated method stub
		return (AudioStream) this.orignalStream;
	}

	@Override
	public AudioStream getOutStream() {
		return (AudioStream) this.destinationStream;
	}

}
