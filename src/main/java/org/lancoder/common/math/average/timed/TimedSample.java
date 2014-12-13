package org.lancoder.common.math.average.timed;

import java.io.Serializable;

public class TimedSample<T> implements Serializable {

	private static final long serialVersionUID = -3208221443819797683L;
	private long timeMSec;
	private T sampleValue;

	public TimedSample(long timeMSec, T sampleValue) {
		this.timeMSec = timeMSec;
		this.sampleValue = sampleValue;
	}

	public long getTimeMSec() {
		return timeMSec;
	}

	public T getSampleValue() {
		return sampleValue;
	}

}
