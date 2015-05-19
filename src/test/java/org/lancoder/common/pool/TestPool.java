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
	public void testWorkerSynchronity() {
		ArrayList<DummyTask> tasks = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			tasks.add(new DummyTask(10));
		}

		PoolWorker<DummyTask> worker = new DummyPoolWorker();
		try {
			for (DummyTask task : tasks) {
				Thread.sleep(11);
				worker.handle(task);
			}
		} catch (InterruptedException e) {
		}
	}

	@Test
	public void testWorkerDeniesTaskIfBusy() {
		DummyTask task1 = new DummyTask(200);
		DummyTask task2 = new DummyTask(200);
		PoolWorker<DummyTask> worker = new DummyPoolWorker();
		Thread workerThread = new Thread(worker);
		workerThread.start();

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}

		assertTrue(worker.handle(task1));
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
		}

		assertTrue(worker.isActive());
		assertFalse(worker.handle(task2));

		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
		}

		assertTrue(worker.handle(task2));
		worker.start();

		assertTrue(task1.completed);
		assertTrue(task2.completed);

	}

	@Test
	public void testDispatchingSingleWorker() {
		DummyTask task1 = new DummyTask();
		DummyTask task2 = new DummyTask();
		DummyTask task3 = new DummyTask();

		pool.setThreadLimit(1);

		assertTrue(pool.hasFreeConverters());

		assertTrue(pool.add(task1));
		assertFalse(pool.hasFreeConverters());

		assertTrue(pool.add(task2));
		assertTrue(pool.add(task3));

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

		for (int i = 0; i < 20; i++) {
			tasks.add(new DummyTask());
		}

		pool.setThreadLimit(10);

		for (DummyTask dummyTask : tasks) {
			pool.add(dummyTask);
		}

		assertTrue(pool.hasWorking());
		assertFalse(pool.hasFreeConverters());

		try {
			Thread.sleep(350);
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
				Thread.sleep(task.ms);
				task.completed = true;
			} catch (Exception e) {
			}
		}

		@Override
		public void serviceFailure(Exception e) {
		}
	}

	class DummyTask {

		public boolean completed = false;
		public int ms = 100;

		public DummyTask() {
		}

		public DummyTask(int ms) {
			this.ms = ms;
		}
	}
}
