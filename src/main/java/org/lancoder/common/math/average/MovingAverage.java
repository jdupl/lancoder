package org.lancoder.common.math.average;

import java.io.Serializable;

import org.apache.commons.collections4.queue.CircularFifoQueue;

public class MovingAverage implements Serializable {

	private static final long serialVersionUID = -7629002197323552955L;

	private CircularFifoQueue<Double> samples;

	/**
	 * Create a moving average with a fixed sample size
	 * 
	 * @param historySize
	 *            The number of samples to keep
	 */
	public MovingAverage(int historySize) {
		this.samples = new CircularFifoQueue<Double>(historySize);
	}

	/**
	 * Add a sample to the moving average
	 * 
	 * @param value
	 *            The value of the sample
	 */
	public void add(double value) {
		this.samples.add(value);
	}

	/**
	 * Calculate current average of the values of the last n samples.
	 * 
	 * @return The average
	 */
	public double getAverage() {
		double total = 0;
		for (Double sample : samples) {
			total += sample;
		}
		return total / samples.size();
	}
}
