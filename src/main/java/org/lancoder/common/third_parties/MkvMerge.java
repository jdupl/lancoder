package org.lancoder.common.third_parties;

import org.lancoder.master.MasterConfig;

public class MkvMerge extends ThirdParty {
	private MasterConfig config;

	public MkvMerge(MasterConfig config) {
		this.config = config;
	}

	@Override
	public String getPath() {
		return config.getMkvMergePath();
	}
}
