package org.lancoder.common.codecs.impl;

import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.codecs.base.VideoCodec;

public class H264 extends VideoCodec {

	protected H264() {
		super(CodecEnum.H264);
	}

	@Override
	public boolean supportsPresets() {
		return true;
	}
}
