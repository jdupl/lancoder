package org.lancoder.common.file_components.streams;

import java.util.ArrayList;
import java.util.Collections;

import org.lancoder.common.codecs.ChannelDisposition;
import org.lancoder.common.codecs.base.AudioCodec;
import org.lancoder.common.job.RateControlType;
import org.lancoder.common.progress.Unit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AudioStream extends Stream {

	private static final long serialVersionUID = 4813380418557482787L;
	private int rate;
	private RateControlType rateControlType = RateControlType.AUTO;
	private ChannelDisposition channels = ChannelDisposition.ORIGINAL;
	private int sampleRate;
	protected Unit unit = Unit.SECONDS;

	public AudioStream(int index, AudioCodec codec, long units, int rate, RateControlType rateControlType,
			ChannelDisposition channels, int sampleRate, Unit unit, String relativeFile) {
		super(index, codec, units, relativeFile);
		this.rate = rate;
		this.rateControlType = rateControlType;
		this.channels = channels;
		this.sampleRate = sampleRate;
		this.unit = unit;
	}

	public AudioStream(JsonObject json, String relativeSource, long unitCount) {
		super(json, relativeSource, unitCount);
		JsonElement element = null;
		// Convert msec to sec
		this.unitCount = unitCount / 1000;
		if ((element = json.get("bit_rate")) != null) {
			// convert from bit/s to kbps
			this.rateControlType = RateControlType.VBR;
			this.rate = element.getAsInt() / 1000;
		}
		if ((element = json.get("channels")) != null) {
			this.channels = ChannelDisposition.getDispositionFromCount(element.getAsInt());
		}
		if ((element = json.get("sample_rate")) != null) {
			this.sampleRate = element.getAsInt();
		}
	}

	public float getRate() {
		return rate;
	}

	public RateControlType getRateControlType() {
		return rateControlType;
	}

	public ChannelDisposition getChannels() {
		return channels;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	@Override
	public ArrayList<String> getStreamCopyMapping() {
		ArrayList<String> args = new ArrayList<>();
		Collections.addAll(args, "-D", "-S", "-B", "--no-chapters", "-M", "--no-global-tags", "-a",
				String.valueOf(this.index));
		return args;
	}
}
