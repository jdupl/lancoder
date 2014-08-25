package org.lancoder.common.codecs;

public enum ChannelDisposition {
	/**
	 * ORIGINAL is used only to keep original channel arrangement
	 */
	ORIGINAL(0), MONO(1), STEREO(2), SURROUND_51(6), SURROUND_71(8);

	protected int count;

	ChannelDisposition(int channelCount) {
		this.count = channelCount;
	}

	public int getCount() {
		return count;
	}

	public static ChannelDisposition getDispositionFromCount(int count) {
		for (ChannelDisposition disposition : ChannelDisposition.values()) {
			if (count == disposition.getCount()) {
				return disposition;
			}
		}
		return ORIGINAL;
	}

}
