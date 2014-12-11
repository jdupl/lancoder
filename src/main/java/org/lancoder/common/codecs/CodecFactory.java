package org.lancoder.common.codecs;

import org.lancoder.common.codecs.base.AbstractCodec;

public class CodecFactory {

	public static AbstractCodec fromString(String codecName) {
		AbstractCodec codec = null;
		CodecEnum codecEnum = CodecEnum.findByLib(codecName);
		return codec;
	}

}
