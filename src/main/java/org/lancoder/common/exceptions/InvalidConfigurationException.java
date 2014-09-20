package org.lancoder.common.exceptions;

public class InvalidConfigurationException extends Exception {

	private static final long serialVersionUID = 8452232793746449187L;

	public InvalidConfigurationException() {
		super("Unknown error");
	}

	public InvalidConfigurationException(String message) {
		super(message);
	}

}
