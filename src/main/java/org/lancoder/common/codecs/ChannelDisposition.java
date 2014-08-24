package org.lancoder.common.codecs;

public enum ChannelDisposition {
	/**
	 * Auto is used only to keep original channel arrangement
	 */
	ORIGINAL(0), MONO(1), STEREO(2), SURROUND_51(6), SURROUND_71(8);

	protected int channels;

	ChannelDisposition(int channels) {
		this.channels = channels;
	}

	public int getChannels() {
		return channels;
	}

}
