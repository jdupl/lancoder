package org.lancoder.common.pool;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestPool {

	DummyPool pool;

	@Before
	public void setup() {
		pool = new DummyPool(1);
		Thread t = new Thread(pool);
		t.start();
	}

	@Test
	public void testDispatchingSingleWorker() {
		assertTrue(pool.hasFreeConverters());
		assertTrue(pool.handle(new Object()));
		assertTrue(pool.hasWorking());
		assertFalse(pool.hasFreeConverters());
		assertTrue(pool.handle(new Object()));
		assertTrue(pool.handle(new Object()));
		try {
			Thread.sleep(350);
		} catch (Exception e) {
		}
		assertFalse(pool.hasWorking());
		assertTrue(pool.hasFreeConverters());
	}

	class DummyPool extends Pool<Object> {

		public DummyPool(int threadLimit) {
			super(threadLimit, true);
		}

		@Override
		protected PoolWorker<Object> getPoolWorkerInstance() {
			return new DummyPoolWorker();
		}

		@Override
		public void serviceFailure(Exception e) {
		}

	}

	class DummyPoolWorker extends PoolWorker<Object> {

		@Override
		protected void start() {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}

		@Override
		public void serviceFailure(Exception e) {
		}

	}
}
