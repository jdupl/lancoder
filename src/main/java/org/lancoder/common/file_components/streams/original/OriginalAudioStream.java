package org.lancoder.common.file_components.streams.original;

import org.lancoder.common.codecs.ChannelDisposition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class OriginalAudioStream extends OriginalStream {

	private static final long serialVersionUID = -1399226113980388391L;
	private ChannelDisposition channels = ChannelDisposition.ORIGINAL;
	private int sampleRate;
	private int bitrate;

	public OriginalAudioStream(JsonObject json, String relativeFile, long unitCount) {
		super(json, relativeFile, unitCount);
		JsonElement element = null;
		// Convert msec to sec
		this.unitCount = unitCount / 1000;
		if ((element = json.get("bit_rate")) != null) {
			// convert from bit/s to kbps
			this.bitrate = element.getAsInt() / 1000;
		}
		if ((element = json.get("channels")) != null) {
			this.channels = ChannelDisposition.getDispositionFromCount(element.getAsInt());
		}
		if ((element = json.get("sample_rate")) != null) {
			this.sampleRate = element.getAsInt();
		}
	}

	public ChannelDisposition getChannels() {
		return channels;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public int getBitrate() {
		return bitrate;
	}

}
