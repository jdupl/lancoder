package org.lancoder.common.file_components.streams;

import org.lancoder.common.strategies.stream.StreamHandlingStrategy;

public class AudioStream extends Stream {

	private static final long serialVersionUID = 4813380418557482787L;

	public AudioStream(StreamHandlingStrategy strategy, String relativeFile, int index) {
		super(strategy, relativeFile, index);
	}

}
