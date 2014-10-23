package org.lancoder.common.third_parties;

import org.lancoder.master.MasterConfig;

public class MkvMerge extends ThirdParty {

	MasterConfig masterConfig;

	public MkvMerge(MasterConfig masterConfig) {
		this.masterConfig = masterConfig;
	}

	@Override
	public String getPath() {
		return masterConfig.getMkvMergePath();
	}

}
