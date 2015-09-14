package org.lancoder.common.codecs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lancoder.common.codecs.base.Codec;
import org.lancoder.common.codecs.impl.Flac;

public class TestCodecLoader {

	@Test
	public void testSameInstance() {
		Codec codecInstance1 = CodecLoader.fromCodec(CodecEnum.FLAC);
		assertTrue(codecInstance1 instanceof Flac);
		Codec codecInstance2 = CodecLoader.fromCodec(CodecEnum.FLAC);
		assertTrue(codecInstance1 == codecInstance2); // Compare pointers
	}

}
