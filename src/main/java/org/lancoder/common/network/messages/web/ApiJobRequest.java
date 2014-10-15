package org.lancoder.common.network.messages.web;

import org.lancoder.common.codecs.ChannelDisposition;
import org.lancoder.common.codecs.Codec;
import org.lancoder.common.job.FFmpegPreset;
import org.lancoder.common.job.RateControlType;

public class ApiJobRequest {

	private String name;
	private String inputFile;
	// Video
	private int rate;
	private int passes;
	private Codec videoCodec;
	private FFmpegPreset preset;
	private RateControlType rateControlType;
	// Audio
	private UserAudioPreset audioConfig;
	private Codec audioCodec;
	private RateControlType audioRateControlType;
	private int audioRate;
	private ChannelDisposition audioChannels;
	private int audioSampleRate;

	public Codec getVideoCodec() {
		return videoCodec;
	}

	public UserAudioPreset getAudioPreset() {
		return audioConfig;
	}

	public void setAudioPreset(UserAudioPreset audioPreset) {
		this.audioConfig = audioPreset;
	}

	public RateControlType getAudioRateControlType() {
		return audioRateControlType;
	}

	public int getAudioRate() {
		return audioRate;
	}

	public void setAudioRate(int audioRate) {
		this.audioRate = audioRate;
	}

	public void setAudioRateControlType(RateControlType audioRateControlType) {
		this.audioRateControlType = audioRateControlType;
	}

	public ChannelDisposition getAudioChannels() {
		return audioChannels;
	}

	public void setAudioChannels(ChannelDisposition audioChannels) {
		this.audioChannels = audioChannels;
	}

	public int getAudioSampleRate() {
		return audioSampleRate;
	}

	public void setAudioSampleRate(int audioSampleRate) {
		this.audioSampleRate = audioSampleRate;
	}

	public Codec getAudioCodec() {
		return audioCodec;
	}

	public void setAudioCodec(Codec audioCodec) {
		this.audioCodec = audioCodec;
	}

	public int getPasses() {
		return passes;
	}

	public void setPasses(int passes) {
		this.passes = passes;
	}

	public FFmpegPreset getPreset() {
		return preset;
	}

	public void setPreset(FFmpegPreset preset) {
		this.preset = preset;
	}

	public RateControlType getRateControlType() {
		return rateControlType;
	}

	public void setRateControlType(RateControlType rateControlType) {
		this.rateControlType = rateControlType;
	}

	public String getInputFile() {
		return inputFile;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}
}