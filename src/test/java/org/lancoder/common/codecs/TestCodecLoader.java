package org.lancoder.common.codecs;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lancoder.common.codecs.base.AbstractCodec;
import org.lancoder.common.codecs.impl.Flac;

public class TestCodecLoader {

	@Test
	public void testSameInstance() {
		AbstractCodec codecInstance1 = CodecLoader.fromCodec(CodecEnum.FLAC);
		assertTrue(codecInstance1 instanceof Flac);
		AbstractCodec codecInstance2 = CodecLoader.fromCodec(CodecEnum.FLAC);
		assertTrue(codecInstance1 == codecInstance2); // Compare pointers
	}

}
