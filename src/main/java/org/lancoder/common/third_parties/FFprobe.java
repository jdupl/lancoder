package org.lancoder.common.third_parties;

import org.lancoder.master.MasterConfig;

public class FFprobe extends ThirdParty {

	private MasterConfig masterConfig;

	public FFprobe(MasterConfig config) {
		masterConfig = config;
	}

	@Override
	public String getPath() {
		return masterConfig.getFFprobePath();
	}

}
