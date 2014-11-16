package org.lancoder.common.file_components.streams;

import java.util.ArrayList;
import java.util.Collections;

import org.lancoder.common.codecs.Codec;
import org.lancoder.common.job.FFmpegPreset;
import org.lancoder.common.job.RateControlType;
import org.lancoder.common.progress.Unit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class VideoStream extends Stream {

	private static final long serialVersionUID = -2445363550218345849L;

	private double frameRate = 0;
	private int rate;
	private RateControlType rateControlType = RateControlType.AUTO;
	private FFmpegPreset preset = FFmpegPreset.MEDIUM;
	private int width = 0;
	private int height = 0;
	private Unit unit = Unit.SECONDS;
	private int stepCount = 1;

	public VideoStream(int index, Codec codec, double frameRate, int rate, RateControlType rateControlType,
			FFmpegPreset preset, int width, int height, long unitCount, Unit unit, int stepCount, String relativeFile) {
		super(index, codec, unitCount, relativeFile);
		this.frameRate = frameRate;
		this.rate = rate;
		this.rateControlType = rateControlType;
		this.preset = preset;
		this.width = width;
		this.height = height;
		this.unit = unit;
		this.stepCount = stepCount;
		if (unit == Unit.SECONDS && this.frameRate > 0) {
			unitCount = (long) (this.frameRate * unitCount);
			this.unit = Unit.FRAMES;
		}
	}

	public VideoStream(JsonObject json, String relativeSource, long unitCount) {
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

	public int getStepCount() {
		return stepCount;
	}

	public double getFrameRate() {
		return frameRate;
	}

	public Unit getUnit() {
		return unit;
	}

	@Override
	public ArrayList<String> getStreamCopyMapping() {
		ArrayList<String> args = new ArrayList<>();
		Collections.addAll(args, "-A", "-S", "-B", "-M", "--no-global-tags");
		return args;
	}

	public FFmpegPreset getPreset() {
		return preset;
	}

	public int getRate() {
		return rate;
	}

	public RateControlType getRateControlType() {
		return rateControlType;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public long getUnitCount() {
		return unitCount;
	}

}
