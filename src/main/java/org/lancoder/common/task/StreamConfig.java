package org.lancoder.common.task;

import java.io.Serializable;
import java.util.ArrayList;

import org.lancoder.common.file_components.streams.Stream;
import org.lancoder.common.file_components.streams.original.OriginalStream;

public abstract class StreamConfig implements Serializable {

	private static final long serialVersionUID = 4111919797278370142L;
	protected String jobId;
	protected ArrayList<String> extraEncoderArgs;
	protected int passes;
	protected OriginalStream orignalStream;
	protected Stream destinationStream;

	public StreamConfig(String jobId, ArrayList<String> extraEncoderArgs, int passes, OriginalStream orignalStream,
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

	public abstract OriginalStream getOrignalStream();

	public abstract Stream getOutStream();
}
