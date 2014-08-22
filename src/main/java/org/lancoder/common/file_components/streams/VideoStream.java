package org.lancoder.common.file_components.streams;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class VideoStream extends Stream {

	private static final long serialVersionUID = -2445363550218345849L;
	private double framerate = 0;
	private int width = 0;
	private int height = 0;

	public VideoStream(JsonObject json) {
		super(json);
		JsonElement element = null;
		if ((element = json.get("r_frame_rate")) != null) {
			// raw frame rate is noted as 24000/1001 or 24/1
			String rawFrameRate = element.getAsString();
			String[] values = rawFrameRate.split("/");
			if (values.length == 2) {
				int decimals = values[1].length() - 1;
				this.framerate = (Double.parseDouble(values[0]) / Double.parseDouble(values[1]));
				this.framerate = Math.floor(this.framerate * Math.pow(10, decimals)) / Math.pow(10, decimals);
			}
		}
		if ((element = json.get("width")) != null) {
			this.width = element.getAsInt();
		}
		if ((element = json.get("height")) != null) {
			this.height = element.getAsInt();
		}
	}

	public double getFramerate() {
		return framerate;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
