package org.lancoder.common.file_components.streams.original;

import org.lancoder.common.progress.Unit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class OriginalVideoStream extends OriginalStream {

	private static final long serialVersionUID = 8783136626130688822L;
	private double frameRate;
	private int width = 0;
	private int height = 0;

	public OriginalVideoStream(JsonObject json, String relativeSource, long unitCount) {
		super(json, relativeSource, unitCount);
		this.unit = Unit.SECONDS;
		JsonElement element = null;
		if ((element = json.get("r_frame_rate")) != null) {
			// raw frame rate is noted as 24000/1001 or 24/1
			String rawFrameRate = element.getAsString();
			String[] values = rawFrameRate.split("/");
			if (values.length == 2) {
				int decimals = values[1].length() - 1;
				this.frameRate = (Double.parseDouble(values[0]) / Double.parseDouble(values[1]));
				this.frameRate = Math.floor(this.frameRate * Math.pow(10, decimals)) / Math.pow(10, decimals);
			}
		}
		if ((element = json.get("width")) != null) {
			this.width = element.getAsInt();
		}
		if ((element = json.get("height")) != null) {
			this.height = element.getAsInt();
		}
		if (frameRate != 0) {
			this.unitCount = (long) (frameRate * this.unitCount);
			this.unit = Unit.FRAMES;
		}
	}

	public double getFrameRate() {
		return frameRate;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
