package org.lancoder.master;

import java.util.ArrayList;

import org.lancoder.common.logging.LogRecord;

public class ClusterLogCollector {

	private final ArrayList<LogRecord> clusterRecords = new ArrayList<>();

	public ArrayList<LogRecord> getAllRecords() {
		return clusterRecords;
	}

	public void add(java.util.logging.LogRecord record) {
		clusterRecords.add(new LogRecord(record));
	}

	public void add(java.util.logging.LogRecord record, String unid) {
		clusterRecords.add(new LogRecord(record, unid));
	}

	public void add(LogRecord record) {
		clusterRecords.add(record);
	}

	@Override
	public String toString() {
		return String.valueOf(clusterRecords.size());
	}

}
