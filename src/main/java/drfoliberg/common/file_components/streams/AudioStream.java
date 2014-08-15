package drfoliberg.common.file_components.streams;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AudioStream extends Stream {

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

}
