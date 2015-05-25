package org.lancoder.common.scheduler;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class SchedulerTest {

	@Test
	public void test() {
		Scheduler s = new Scheduler();
		Thread t = new Thread(s);
		s.setThread(t);
		t.start();

		DummySchedulable schedulable = new DummySchedulable();
		s.addSchedulable(schedulable);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(1, schedulable.changeMe);
	}

}

class DummySchedulable extends Schedulable {

	public int changeMe = 0;

	@Override
	public void runTask() {
		System.out.println("ok");
		changeMe++;
		System.out.println(changeMe);
	}
}