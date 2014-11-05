package org.lancoder.common.math.average.timed;

public class TimedSample<T> {

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
