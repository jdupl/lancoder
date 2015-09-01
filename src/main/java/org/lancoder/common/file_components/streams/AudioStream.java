package org.lancoder.common.file_components.streams;

import org.lancoder.common.file_components.streams.original.OriginalStream;
import org.lancoder.common.strategies.stream.StreamHandlingStrategy;

public class AudioStream extends Stream {

	private static final long serialVersionUID = 4813380418557482787L;

	public AudioStream(StreamHandlingStrategy strategy, OriginalStream originalStream, int index) {
		super(strategy, originalStream, index);
	}

	@Override
	public String getMkvMergeStreamTypeArg() {
		return "a";
	}

}
