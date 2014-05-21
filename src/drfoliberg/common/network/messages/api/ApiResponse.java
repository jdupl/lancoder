package drfoliberg.common.network.messages.api;

import java.io.Serializable;

public class ApiResponse implements Serializable {

	private static final long serialVersionUID = 2890084482426925083L;

	private boolean success;
	private String message;

	public ApiResponse(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
