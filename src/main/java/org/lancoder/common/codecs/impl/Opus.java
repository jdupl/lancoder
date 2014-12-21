package org.lancoder.common.codecs.impl;

import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.codecs.base.AudioCodec;

public class Opus extends AudioCodec {

	private static final long serialVersionUID = -6605417518271536556L;

	protected Opus() {
		super(CodecEnum.OPUS);
	}

	@Override
	public String getCRFSwitchArg() {
		return getVBRSwitchArg();
	}

	/**
	 * Opus does not support quality based encoding...
	 */
	@Override
	public String formatQuality(int rate) {
		return formatBitrate(96);
	}

	@Override
	public String formatHz(int originalFrenquency) {
		int[] frequencies = new int[] { 8000, 12000, 16000, 24000, 48000 };
		int closestValue = Integer.MAX_VALUE;

		for (int frenquency : frequencies) {
			int diff = Math.abs(frenquency - originalFrenquency);
			if (diff < Math.abs(closestValue - originalFrenquency)) {
				closestValue = frenquency;
			}
		}
		return String.valueOf(closestValue);
	}
}
