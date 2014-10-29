package org.lancoder.common.events;

import org.lancoder.common.network.cluster.messages.StatusReport;

public class Event {

	private Object object;
	private EventEnum code;

	public Event(EventEnum code, Object object) {
		this.object = object;
		this.code = code;
	}

	public Event(StatusReport report) {
		this(EventEnum.STATUS_REPORT, report);
	}

	public Event(EventEnum code) {
		this.code = code;
	}

	public Object getObject() {
		return object;
	}

	public EventEnum getCode() {
		return code;
	}

}
