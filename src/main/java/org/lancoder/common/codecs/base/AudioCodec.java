package org.lancoder.common.codecs.base;

import org.lancoder.common.codecs.CodecEnum;

public abstract class AudioCodec extends AbstractCodec {

	public AudioCodec(CodecEnum codecEnum) {
		super(codecEnum);
	}

	@Override
	public String getTypeSwitch() {
		return "a";
	}

}
