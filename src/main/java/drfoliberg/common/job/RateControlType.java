package drfoliberg.common.job;

import java.io.Serializable;

public enum RateControlType implements Serializable {

	/**
	 * Please refer to https://mailman.videolan.org/pipermail/x264-devel/2010-February/006933.html
	 */

	VBR(2), CRF(1); // , CQP

	int maxPass;

	RateControlType(int maxPass) {
		this.maxPass = maxPass;
	}

	public int getMaxPass() {
		return this.maxPass;
	}

}
