package main.java.drfoliberg.common.network.messages.cluster;

import java.io.Serializable;

public class Message implements Serializable {

	public String getPath() {
		return path;
	}

	private static final long serialVersionUID = -483657531000641905L;

	protected String path;

	public Message(String path) {
		this.path = path;
	}

}
