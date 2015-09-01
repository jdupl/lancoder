package org.lancoder.common.third_parties;

import java.io.IOException;

public abstract class ThirdParty {

	private boolean installed = false;
	protected boolean required = true;

	public ThirdParty() {
		this(true);
	}

	public ThirdParty(boolean required) {
		this.required = required;
	}

	public abstract String getPath();

	/**
	 * Check if third party is installed.
	 *
	 * @return True if process could be created.
	 */
	public boolean isInstalled() {
		if (installed) {
			return true;
		}

		ProcessBuilder pb = new ProcessBuilder(getPath());
		try {
			pb.start();
		} catch (IOException e) {
			return false;
		}
		installed = true;

		return installed;
	}

	public boolean isRequired() {
		return required;
	}

}
