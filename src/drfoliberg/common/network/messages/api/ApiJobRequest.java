package drfoliberg.common.network.messages.api;

import drfoliberg.common.job.FFmpegPreset;
import drfoliberg.common.job.RateControlType;

public class ApiJobRequest {

	private String name;
	private String inputFile;
	private int rate;
	private byte passes;
	private FFmpegPreset preset;
	private RateControlType rateControlType;

	public byte getPasses() {
		return passes;
	}

	public void setPasses(byte passes) {
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