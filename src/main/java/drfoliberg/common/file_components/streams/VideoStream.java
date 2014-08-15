package drfoliberg.common.file_components.streams;

import com.google.gson.JsonObject;

public class VideoStream extends Stream {
	private float bitrate;
	private float framerate;
	private int width;
	private int height;

	public VideoStream(JsonObject json) {
		super(json);
	}
}
