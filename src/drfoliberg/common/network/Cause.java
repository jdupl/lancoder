package drfoliberg.common.network;

public class Cause {
	
	/**
	 * The exception that occurred.
	 * null if none
	 */
	private Exception exception;
	
	/**
	 * Additional message
	 */
	private String message;
	
	/**
	 * Is this error fatal i.e. will another job fail ? 
	 */
	private boolean fatal;

	public Cause(Exception exception, String message, boolean fatal) {
		this.exception = exception;
		this.message = message;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isFatal() {
		return fatal;
	}

	public void setFatal(boolean fatal) {
		this.fatal = fatal;
	}

}
