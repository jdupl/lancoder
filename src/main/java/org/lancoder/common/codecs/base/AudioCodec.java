package org.lancoder.common.codecs.base;

import org.lancoder.common.codecs.CodecEnum;

public abstract class AudioCodec extends Codec {

	private static final long serialVersionUID = -6436301305230944537L;

	public AudioCodec(CodecEnum codecEnum) {
		super(codecEnum);
	}

	@Override
	public String getTypeSwitch() {
		return "a";
	}

}
