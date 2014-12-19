package org.lancoder.common.task.video;

import java.util.ArrayList;

import org.lancoder.common.file_components.streams.VideoStream;
import org.lancoder.common.file_components.streams.original.OriginalVideoStream;
import org.lancoder.common.task.StreamConfig;

public class VideoStreamConfig extends StreamConfig {

	private static final long serialVersionUID = -1258560811697509098L;

	public VideoStreamConfig(String jobId, ArrayList<String> extraEncoderArgs, int passes, OriginalVideoStream orignalStream,
			VideoStream destinationStream) {
		super(jobId, extraEncoderArgs, passes, orignalStream, destinationStream);
	}

	@Override
	public OriginalVideoStream getOrignalStream() {
		return (OriginalVideoStream) this.orignalStream;
	}

	@Override
	public VideoStream getOutStream() {
		return (VideoStream) this.destinationStream;
	}

}
