package org.lancoder.common.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.lancoder.master.ClusterLogCollector;

/**
 * Collects logs from the master instance.
 *
 */

public class LogCollectorHandler extends Handler {

	private ClusterLogCollector collector;

	public LogCollectorHandler(ClusterLogCollector collector) {
		super();
		this.collector = collector;
	}

	@Override
	public void publish(LogRecord record) {
		collector.add(record);
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}
}
