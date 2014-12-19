package org.lancoder.common.codecs.impl;

import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.codecs.base.AudioCodec;

public class Opus extends AudioCodec {

	private static final long serialVersionUID = -6605417518271536556L;

	protected Opus() {
		super(CodecEnum.OPUS);
	}

	@Override
	public String getCRFSwitch() {
		return "compression_level";
	}
}
