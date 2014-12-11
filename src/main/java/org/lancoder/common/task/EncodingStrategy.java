package org.lancoder.common.task;

import org.lancoder.common.codecs.base.AbstractCodec;
import org.lancoder.common.job.RateControlType;

public class EncodingStrategy {

	private AbstractCodec codec;
	private RateControlType rateControlType;

	public EncodingStrategy(AbstractCodec codec, RateControlType rateControlType) {
		this.codec = codec;
		this.rateControlType = rateControlType;
	}

	public String getRateControlSwitch() {
		String s = rateControlType == RateControlType.VBR ? codec.getBitrateSwitch() : codec.getCRFSwitch();
		return String.format("-%s:%s", s, codec.getTypeSwitch());
	}

}
