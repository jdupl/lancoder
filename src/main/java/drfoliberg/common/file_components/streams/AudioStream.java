package drfoliberg.common.file_components.streams;

import com.google.gson.JsonObject;

public class AudioStream extends Stream {

	private float bitrate;
	private int channels;
	private int sampleRate;

	public AudioStream(JsonObject json) {
		super(json);
	}
}
