package org.lancoder.common.scheduler;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lancoder.common.scheduler.Scheduler;
import org.lancoder.common.scheduler.Schedulable;

public class SchedulerTest {

	@Test
	public void testSingleSchedulable() {
		Scheduler s = new Scheduler();
		Thread t = new Thread(s);

		s.setThread(t);
		t.start();

		DummySchedulable schedulable = new DummySchedulable();
		s.addSchedulable(schedulable);

		try {
			Thread.sleep(5 * 100 + 10);
		} catch (InterruptedException e) {
		}

		assertEquals(5, schedulable.changeMe);
	}

	@Test
	public void testLimitedSchedulable() {
		Scheduler s = new Scheduler();
		Thread t = new Thread(s);

		s.setThread(t);
		t.start();

		DummySchedulable schedulable = new DummySchedulable();
		schedulable.maxCount = 5;
		s.addSchedulable(schedulable);

		try {
			Thread.sleep(700);
		} catch (InterruptedException e) {
		}

		assertEquals(5, schedulable.changeMe);
	}

	@Test
	public void testCompareTo() {
		long runAt = 1000000;

		DummySchedulable first = new DummySchedulable();
		first.nextRun = runAt;

		DummySchedulable second = new DummySchedulable();
		second.nextRun = runAt + 1000;

		assertTrue(first.compareTo(second) < 0);
	}

	@Test
	public void testMultipleSchedulables() {
		Scheduler s = new Scheduler();
		Thread t = new Thread(s);

		s.setThread(t);
		t.start();

		DummySchedulable schedulable1 = new DummySchedulable(250);
		s.addSchedulable(schedulable1);

		DummySchedulable schedulable2 = new DummySchedulable(500);
		s.addSchedulable(schedulable2);

		DummySchedulable schedulable3 = new DummySchedulable(3000);
		s.addSchedulable(schedulable3);

		try {
			Thread.sleep(2200);
		} catch (InterruptedException e) {
		}

		assertTrue(schedulable1.changeMe >= 8);
		assertTrue(schedulable2.changeMe >= 4);
		assertEquals(0, schedulable3.changeMe);
	}
}

class DummySchedulable extends Schedulable {

	public int changeMe = 0;
	private int msDelay;

	public DummySchedulable(int delay) {
		this.msDelay = delay;
	}

	public DummySchedulable() {
		this.msDelay = 100;
	}

	@Override
	public void runTask() {
		changeMe++;
	}

	@Override
	public long getMsRunDelay() {
		return msDelay;
	}

}