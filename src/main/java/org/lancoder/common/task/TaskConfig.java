package org.lancoder.common.task;

import java.io.Serializable;
import java.util.ArrayList;

import org.lancoder.common.codecs.Codec;
import org.lancoder.common.job.RateControlType;

public class TaskConfig implements Serializable {

	private static final long serialVersionUID = -8201664961243820323L;

	protected RateControlType rateControlType;
	protected int rate; // kbps or crf TODO use BiterateControl ?
	protected int passes;
	protected Codec codec;
	protected ArrayList<String> extraEncoderArgs; // TODO usage this to allow --slow-first-pass and other overrides

	public TaskConfig(RateControlType rateControlType, int rate, int passes, Codec codec, ArrayList<String> extraEncoderArgs) {
		this.rateControlType = rateControlType;
		this.rate = rate;
		this.passes = passes;
		this.codec = codec;
		this.extraEncoderArgs = extraEncoderArgs;
	}

	public Codec getCodec() {
		return codec;
	}

	public int getPasses() {
		return passes;
	}

	public void setPasses(int passes) {
		this.passes = passes;
	}

	public RateControlType getRateControlType() {
		return rateControlType;
	}

	public void setRateControlType(RateControlType rateControlType) {
		this.rateControlType = rateControlType;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public ArrayList<String> getExtraEncoderArgs() {
		return extraEncoderArgs;
	}

	public void setExtraEncoderArgs(ArrayList<String> extraEncoderArgs) {
		this.extraEncoderArgs = extraEncoderArgs;
	}

}