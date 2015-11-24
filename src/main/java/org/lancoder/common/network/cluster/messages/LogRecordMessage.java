package org.lancoder.common.network.cluster.messages;

import java.util.logging.LogRecord;

import org.lancoder.common.network.cluster.protocol.ClusterProtocol;

public class LogRecordMessage extends AuthMessage {

	private static final long serialVersionUID = -6782727919481859948L;

	private LogRecord logRecord;

	public LogRecordMessage(LogRecord logRecord, String unid) {
		super(ClusterProtocol.LOG_RECORD, unid);
		this.logRecord = logRecord;
	}

	public LogRecord getLogRecord() {
		return logRecord;
	}

}
