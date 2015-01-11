package org.lancoder.common.codecs.impl;

import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.codecs.base.AudioCodec;

public class Wavpack extends AudioCodec {

	private static final long serialVersionUID = -5387724686772780145L;

	protected Wavpack() {
		super(CodecEnum.WAVPACK);
	}

}
