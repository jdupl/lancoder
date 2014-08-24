package org.lancoder.common.file_components.streams;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AudioStream extends Stream {

	private static final long serialVersionUID = 4813380418557482787L;
	private float bitrate;
	private int channels;
	private int sampleRate;

	public AudioStream(JsonObject json) {
		super(json);
		JsonElement element = null;
		if ((element = json.get("bit_rate")) != null) {
			// convert from bit/s to kbps
			this.bitrate = element.getAsInt() / 1000;
		}
		if ((element = json.get("channels")) != null) {
			this.channels = element.getAsInt();
		}
		if ((element = json.get("sample_rate")) != null) {
			this.sampleRate = element.getAsInt();
		}
	}

	public float getBitrate() {
		return bitrate;
	}

	public int getChannels() {
		return channels;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	@Override
	public ArrayList<String> getStreamCopyMapping() {
		ArrayList<String> args = new ArrayList<>();
		Collections.addAll(args, "-D", "-S", "-B", "--no-chapters", "-M", "--no-global-tags");
		return args;
	}
}
