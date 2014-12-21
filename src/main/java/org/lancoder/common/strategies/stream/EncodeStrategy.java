package org.lancoder.common.strategies.stream;

import java.util.ArrayList;

import org.lancoder.common.codecs.base.AbstractCodec;
import org.lancoder.common.job.RateControlType;

public abstract class EncodeStrategy extends StreamHandlingStrategy {

	private static final long serialVersionUID = -7078509107858295060L;
	protected AbstractCodec codec;
	protected RateControlType rateControlType;
	protected int rate;

	public EncodeStrategy(AbstractCodec codec, RateControlType rateControlType, int rate) {
		this.codec = codec;
		this.rateControlType = rateControlType;
		this.rate = rate;
	}

	public ArrayList<String> getRateControlArgs() {
		String rateControlArg = rateControlType == RateControlType.VBR ? codec.getVBRSwitchArg() : codec
				.getCRFSwitchArg();
		String rateArg = rateControlType == RateControlType.VBR ? codec.formatBitrate(this.rate) : codec.formatQuality(this.rate);
		ArrayList<String> args = new ArrayList<String>();
		args.add(rateControlArg);
		args.add(rateArg);
		return args;
	}

	public AbstractCodec getCodec() {
		return codec;
	}

	public RateControlType getRateControlType() {
		return rateControlType;
	}

	public int getRate() {
		return rate;
	}

}
