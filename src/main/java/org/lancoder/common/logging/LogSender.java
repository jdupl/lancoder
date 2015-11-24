package org.lancoder.common.logging;

import java.util.logging.LogRecord;

/**
 * Interface used to send logs from a worker to it's master.
 */

public interface LogSender {

	public boolean send(LogRecord record);

	public boolean isOnline();
}
