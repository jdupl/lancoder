package org.lancoder.common.task;

import java.io.Serializable;
import java.util.ArrayList;

import org.lancoder.common.job.RateControlType;

public class TaskConfig implements Serializable {

	private static final long serialVersionUID = -8201664961243820323L;

	protected String sourceFile;
	protected RateControlType rateControlType;
	protected int rate; // kbps or crf TODO use BiterateControl ?
	protected int passes;
	protected ArrayList<String> extraEncoderArgs; // TODO usage this to allow --slow-first-pass and other overrides

	public TaskConfig(String sourceFile, RateControlType rateControlType, int rate, int passes,
			ArrayList<String> extraEncoderArgs) {
		this.sourceFile = sourceFile;
		this.rateControlType = rateControlType;
		this.rate = rate;
		this.passes = passes;
		this.extraEncoderArgs = extraEncoderArgs;
	}

//	public JobConfig(JobConfig config) {
//		this.sourceFile = config.sourceFile;
//		this.rateControlType = config.rateControlType;
//		this.rate = config.rate;
//		this.passes = config.passes;
//		this.extraEncoderArgs = config.extraEncoderArgs;
//	}

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

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
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