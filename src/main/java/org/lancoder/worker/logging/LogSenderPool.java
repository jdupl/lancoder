package org.lancoder.worker.logging;

import java.util.logging.LogRecord;

import org.lancoder.common.logging.LogSender;
import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.PoolWorker;
import org.lancoder.common.status.NodeState;
import org.lancoder.worker.Worker;

public class LogSenderPool extends Pool<LogRecord> implements LogSender {

	private Worker worker;

	public LogSenderPool(Worker worker) {
		super(1);
		this.worker = worker;
	}

	@Override
	public boolean send(LogRecord record) {
		return add(record);
	}

	@Override
	public boolean isOnline() {
		return worker.getStatus() != NodeState.NOT_CONNECTED;
	}

	@Override
	protected PoolWorker<LogRecord> getPoolWorkerInstance() {
		return new LogSenderWorker(worker);
	}

}
