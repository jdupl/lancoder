package org.lancoder.common.task;

import java.util.ArrayList;

import org.lancoder.common.file_components.streams.Stream;

public abstract class StreamConfig {

	protected String jobId;
	protected ArrayList<String> extraEncoderArgs;
	protected int passes;
	protected Stream orignalStream;
	protected Stream destinationStream;

	public StreamConfig(String jobId, ArrayList<String> extraEncoderArgs, int passes, Stream orignalStream,
			Stream destinationStream) {
		this.jobId = jobId;
		this.extraEncoderArgs = extraEncoderArgs;
		this.passes = passes;
		this.orignalStream = orignalStream;
		this.destinationStream = destinationStream;
	}

	public String getJobId() {
		return jobId;
	}

	public ArrayList<String> getExtraEncoderArgs() {
		return extraEncoderArgs;
	}

	public int getPasses() {
		return passes;
	}

	public abstract Stream getOrignalStream();

	public abstract Stream getOutStream();
}
