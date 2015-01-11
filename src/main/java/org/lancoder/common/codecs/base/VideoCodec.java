package org.lancoder.common.codecs.base;

import org.lancoder.common.codecs.CodecEnum;

public class VideoCodec extends AbstractCodec {

	private static final long serialVersionUID = -1517396854687235753L;

	public VideoCodec(CodecEnum codecEnum) {
		super(codecEnum);
	}

	@Override
	public String getTypeSwitch() {
		return "v";
	}

	@Override
	public String getCRFSwitchArg() {
		return "-crf";
	}

}
