package org.lancoder.common.logging;

import java.io.Serializable;
import java.util.UUID;

/**
 * Encapsulates LogRecord from stdlib. Allows tracking records by node.
 *
 */
public class LogRecord implements Serializable {

	private static final long serialVersionUID = -9071858228487842900L;

	/**
	 * Unique Node ID from which the record originates
	 */
	private String unid;

	/**
	 * The encapsulated original log record
	 */
	private java.util.logging.LogRecord originalRecord;

	/**
	 * Unique log identifier. Allows user to "read" and delete log entries. Original java.util.logging unique identifier
	 * is node specific.
	 */
	private UUID uuid;

	public LogRecord(java.util.logging.LogRecord record, String unid) {
		this.originalRecord = record;
		this.unid = unid;
		this.uuid = UUID.randomUUID();
	}

	public LogRecord(java.util.logging.LogRecord record) {
		this.originalRecord = record;
		this.unid = null; // master has no unid. TODO FIX ME
		this.uuid = UUID.randomUUID();
	}

	/**
	 *
	 * @return The node from which the record originates
	 */
	public String getUnid() {
		return unid;
	}

	public UUID getUuid() {
		return uuid;
	}

	public java.util.logging.LogRecord getOriginalRecord() {
		return originalRecord;
	}

	@Override
	public String toString() {
		return "LogRecord [unid=" + unid + ", originalRecord=" + originalRecord + ", uuid=" + uuid + "]";
	}

}
