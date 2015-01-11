package org.lancoder.common.pool;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class TestPool {

	DummyPool pool;

	@Before
	public void setup() {
		pool = new DummyPool(1);
		Thread t = new Thread(pool);
		t.start();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDispatchingSingleWorker() {
		DummyTask task1 = new DummyTask();
		DummyTask task2 = new DummyTask();
		DummyTask task3 = new DummyTask();
		pool.setThreadLimit(1);
		assertTrue(pool.hasFreeConverters());
		assertTrue(pool.handle(task1));
		assertFalse(pool.hasFreeConverters());
		assertTrue(pool.handle(task2));
		assertTrue(pool.handle(task3));
		try {
			Thread.sleep(350);
		} catch (Exception e) {
		}
		assertFalse(pool.hasWorking());
		assertTrue(pool.hasFreeConverters());
		assertTrue(task1.completed);
		assertTrue(task2.completed);
		assertTrue(task3.completed);
	}

	@Test
	public void testDispatchingMultipleWorkers() {
		ArrayList<DummyTask> tasks = new ArrayList<>();
		pool.setThreadLimit(10);
		for (int i = 0; i < 20; i++) {
			tasks.add(new DummyTask());
		}
		for (DummyTask dummyTask : tasks) {
			pool.handle(dummyTask);
		}
		assertTrue(pool.hasWorking());
		assertFalse(pool.hasFreeConverters());
		try {
			Thread.sleep(250);
		} catch (Exception e) {
		}
		assertFalse(pool.hasWorking());
		for (DummyTask dummyTask : tasks) {
			assertTrue(dummyTask.completed);
		}
	}

	class DummyPool extends Pool<DummyTask> {

		public DummyPool(int threadLimit) {
			super(threadLimit, true);
		}

		@Override
		protected PoolWorker<DummyTask> getPoolWorkerInstance() {
			return new DummyPoolWorker();
		}

		@Override
		public void serviceFailure(Exception e) {
		}

	}

	class DummyPoolWorker extends PoolWorker<DummyTask> {

		@Override
		protected void start() {
			try {
				Thread.sleep(100);
				task.completed = true;
			} catch (Exception e) {
			}
			this.active = false;
		}

		@Override
		public void serviceFailure(Exception e) {
		}
	}

	class DummyTask {
		public boolean completed = false;
	}
}
