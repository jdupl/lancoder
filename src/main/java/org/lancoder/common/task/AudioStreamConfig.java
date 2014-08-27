package org.lancoder.common.task;

import java.util.ArrayList;

import org.lancoder.common.file_components.streams.AudioStream;

public class AudioStreamConfig extends StreamConfig {

	public AudioStreamConfig(String jobId, ArrayList<String> extraEncoderArgs, int passes,
			AudioStream orignalStream, AudioStream destinationStream) {
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
