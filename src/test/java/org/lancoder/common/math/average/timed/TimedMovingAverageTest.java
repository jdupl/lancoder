package org.lancoder.common.math.average.timed;

import static org.junit.Assert.*;

import org.junit.Test;

public class TimedMovingAverageTest {

	@Test
	public void TestAdd() {
		TimedMovingAverage avg = new TimedMovingAverage(10 * 1000);
		avg.add(10, 0);
		avg.add(20, 0);
		assertEquals(2, avg.getSampleCount());
	}

	@Test
	public void TestTrimFromCount() {
		int limit = 2;
		TimedMovingAverage avg = new TimedMovingAverage(10 * 1000, limit);
		avg.add(10, Long.MAX_VALUE);
		avg.add(20, Long.MAX_VALUE);
		avg.add(30, Long.MAX_VALUE);
		avg.add(40, Long.MAX_VALUE);
		avg.getAverage(); // cleans with count
		assertEquals(limit, avg.getSampleCount());
	}

	@Test
	public void TestTrimFromTime() {
		TimedMovingAverage avg = new TimedMovingAverage(10 * 1000);
		avg.add(10, 0);
		avg.add(20, 0);
		avg.add(30, Long.MAX_VALUE);
		avg.add(40, Long.MAX_VALUE);
		avg.getAverage(); // cleans from time and count
		assertEquals(2, avg.getSampleCount());
	}

}
