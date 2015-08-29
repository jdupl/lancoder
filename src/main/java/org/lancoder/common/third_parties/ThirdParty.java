package org.lancoder.common.third_parties;

import java.io.IOException;

public abstract class ThirdParty {

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
		boolean installed = true;
		ProcessBuilder pb = new ProcessBuilder(getPath());
		try {
			pb.start();
		} catch (IOException e) {
			installed = false;
		}
		return installed;
	}

	public boolean isRequired() {
		return required;
	}

}
