package org.lancoder.common.math.average.timed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Provides a moving average of maximum n elements which expire after m milliseconds
 *
 * @author justin
 *
 */
public class TimedMovingAverage implements Serializable {

	private static final long serialVersionUID = 7322472221784144707L;
	private static final long DEFAULT_MAXIMUM_SAMPLES = 1000;

	private final LinkedList<TimedSample<Double>> samples = new LinkedList<>();
	private long expireTime;
	private long maxSampleCount = DEFAULT_MAXIMUM_SAMPLES;

	/**
	 * Create a timed based average with a 1000 samples maximum.
	 *
	 * @param expireTime
	 *            The maximum time in milliseconds a sample is kept
	 */
	public TimedMovingAverage(long expireTime) {
		this.expireTime = expireTime;
	}

	/**
	 * Create a timed based average.
	 *
	 * @param expireTime
	 *            The maximum time in milliseconds a sample is kept
	 * @param maxSampleCount
	 *            The maximum number of samples to keep
	 */
	public TimedMovingAverage(long expireTime, long maxSampleCount) {
		this(expireTime);
		this.maxSampleCount = maxSampleCount;
	}

	public void add(double sample, long time) {
		if (samples.size() >= maxSampleCount) {
			clean();
		}
		this.samples.add(new TimedSample<Double>(time, sample));
	}

	public void clear() {
		this.samples.clear();
	}

	public double getAverage() {
		double total = 0;
		clean();

		for (TimedSample<Double> sample : samples) {
			total += sample.getSampleValue();
		}
		return total / samples.size();
	}

	private void clean() {
		if (samples.size() > maxSampleCount) {
			trimFromCount();
		}
		if (System.currentTimeMillis() - samples.getFirst().getTimeMSec() > expireTime) {
			trimFromTime();
		}
	}

	private void trimFromTime() {
		ArrayList<TimedSample<Double>> expired = new ArrayList<>();
		long currentMSec = System.currentTimeMillis();

		// Find expired elements
		for (TimedSample<Double> timedSample : samples) {
			if (currentMSec - timedSample.getTimeMSec() > expireTime) {
				expired.add(timedSample);
			}
		}
		// Safely delete elements
		for (TimedSample<Double> timedSample : expired) {
			samples.remove(timedSample);
		}
	}

	private void trimFromCount() {
		while (samples.size() > maxSampleCount) {
			samples.removeFirst();
		}
	}

	public long getSampleCount() {
		return this.samples.size();
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public void setMaxSampleCount(long maxSampleCount) {
		this.maxSampleCount = maxSampleCount;
	}

}
