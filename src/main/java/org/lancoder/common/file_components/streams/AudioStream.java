package org.lancoder.common.file_components.streams;

import java.util.ArrayList;
import java.util.Collections;

import org.lancoder.common.codecs.ChannelDisposition;
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
	

	public AudioStream(JsonObject json, String relativeSource, long unitCount) {
		super(json, relativeSource, unitCount);
		JsonElement element = null;
		this.unitCount = unitCount;
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
		Collections.addAll(args, "-D", "-S", "-B", "--no-chapters", "-M", "--no-global-tags");
		return args;
	}
}
