package org.lancoder.common.task.video;

import java.util.ArrayList;

import org.lancoder.common.file_components.streams.VideoStream;
import org.lancoder.common.task.StreamConfig;

public class VideoStreamConfig extends StreamConfig {

	public VideoStreamConfig(String jobId, ArrayList<String> extraEncoderArgs, int passes,
			VideoStream orignalStream, VideoStream destinationStream) {
		super(jobId, extraEncoderArgs, passes, orignalStream, destinationStream);
	}

	@Override
	public VideoStream getOrignalStream() {
		return (VideoStream) this.orignalStream;
	}

	@Override
	public VideoStream getOutStream() {
		return (VideoStream) this.destinationStream;
	}

}
