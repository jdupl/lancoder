package org.lancoder.common.third_parties;

import org.lancoder.common.config.Config;

public class FFmpeg extends ThirdParty {

	private Config config;

	public FFmpeg(Config config) {
		this.config = config;
	}

	@Override
	public String getPath() {
		return config.getFFmpegPath();
	}

}
