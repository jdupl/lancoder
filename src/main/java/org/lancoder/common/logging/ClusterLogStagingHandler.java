package org.lancoder.common.logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Stages worker logs entries to be sent to master via the cluster protocol
 *
 */
public class ClusterLogStagingHandler extends Handler {

	private final ArrayList<LogRecord> recordsToSend = new ArrayList<>();
	private LogSender logSender;

	public ClusterLogStagingHandler(LogSender sender) {
		this.logSender = sender;
	}

	@Override
	public void publish(LogRecord record) {
		recordsToSend.add(record);
		flush();
	}

	@Override
	public void flush() {
		if (logSender.isOnline()) {
			Iterator<LogRecord> it = recordsToSend.iterator();

			while (it.hasNext()) {
				LogRecord record = it.next();

				if (logSender.send(record)) {
					it.remove();
				} else {
					// something has gone wrong
					break;
				}
			}
		}
	}

	@Override
	public void close() throws SecurityException {
		flush();
		recordsToSend.clear();
	}
}
