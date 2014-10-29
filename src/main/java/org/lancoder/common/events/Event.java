package org.lancoder.common.events;

public class Event {

	private Object object;
	private EventEnum code;

	public Event(EventEnum code, Object object) {
		this.object = object;
		this.code = code;
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
