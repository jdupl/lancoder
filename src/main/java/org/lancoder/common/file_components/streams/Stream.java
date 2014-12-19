package org.lancoder.common.file_components.streams;

import java.util.ArrayList;

import org.lancoder.common.codecs.base.AbstractCodec;
import org.lancoder.common.strategies.stream.EncodeStrategy;
import org.lancoder.common.strategies.stream.StreamHandlingStrategy;

public abstract class Stream extends BaseStream {

	private static final long serialVersionUID = -1867430611531693710L;

	protected StreamHandlingStrategy strategy;

	public Stream(StreamHandlingStrategy strategy, String relativeFile, int index) {
		this.strategy = strategy;
		this.relativeFile = relativeFile;
		this.index = index;
	}

	public StreamHandlingStrategy getStrategy() {
		return strategy;
	}

	@Override
	public AbstractCodec getCodec() {
		AbstractCodec codec = null;
		if (strategy instanceof EncodeStrategy) {
			codec = ((EncodeStrategy) strategy).getCodec();
		}
		return codec;
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
