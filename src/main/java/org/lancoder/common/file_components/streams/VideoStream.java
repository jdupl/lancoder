package org.lancoder.common.file_components.streams;

import org.lancoder.common.file_components.streams.original.OriginalVideoStream;
import org.lancoder.common.strategies.stream.VideoEncodeStrategy;

public class VideoStream extends Stream {

	private static final long serialVersionUID = -2445363550218345849L;

	public VideoStream(VideoEncodeStrategy strategy, OriginalVideoStream originalStream, int index) {
		super(strategy, originalStream, index);
	}

	@Override
	public String getMkvMergeStreamTypeArg() {
		return "v";
	}

}
