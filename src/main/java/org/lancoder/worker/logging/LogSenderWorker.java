package org.lancoder.worker.logging;

import java.util.logging.LogRecord;

import org.lancoder.common.network.MessageSender;
import org.lancoder.common.network.cluster.messages.LogRecordMessage;
import org.lancoder.common.pool.PoolWorker;
import org.lancoder.worker.Worker;

public class LogSenderWorker extends PoolWorker<LogRecord> {

	private Worker worker;

	public LogSenderWorker(Worker worker) {
		super();
		this.worker = worker;
	}

	@Override
	protected void start() {
		LogRecordMessage message = new LogRecordMessage(task, worker.getConfig().getUniqueID());
		MessageSender.send(message, worker.getMasterInetAddress(), worker.getMasterPort());
	}

}
