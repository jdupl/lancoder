package org.lancoder.common.file_components.streams;

import java.util.ArrayList;

import org.lancoder.common.file_components.streams.original.BaseStream;
import org.lancoder.common.file_components.streams.original.OriginalStream;
import org.lancoder.common.strategies.stream.StreamHandlingStrategy;

public abstract class Stream extends BaseStream {

	private static final long serialVersionUID = -1867430611531693710L;

	protected StreamHandlingStrategy strategy;

	public Stream(StreamHandlingStrategy strategy, OriginalStream originalStream, int index) {
		super(originalStream.getRelativeFile(), index, strategy.getCodec(), originalStream.getUnitCount(), originalStream.getUnit());
		this.strategy = strategy;
		this.codec = strategy.getCodec();
	}

	public StreamHandlingStrategy getStrategy() {
		return strategy;
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj != null && obj instanceof Stream) {
			Stream other = (Stream) obj;
			return other.index == this.index && other.codec.equals(this.codec);
		}
		return false;
	}

	public ArrayList<String> getRateControlArgs() {
		return strategy.getRateControlArgs();
	}

}
