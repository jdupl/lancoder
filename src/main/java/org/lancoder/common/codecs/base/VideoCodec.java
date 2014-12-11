package org.lancoder.common.codecs.base;

import org.lancoder.common.codecs.CodecEnum;

public class VideoCodec extends AbstractCodec {

	public VideoCodec(CodecEnum codecEnum) {
		super(codecEnum);
	}

	@Override
	public String getTypeSwitch() {
		return "v";
	}

}
