package org.lancoder.muxer;

import org.lancoder.common.job.Job;
import org.lancoder.common.third_parties.ThirdParty;

public abstract class Muxer {

	public abstract ThirdParty getMuxingThirdParty();

	public abstract void handle(Job job);
}
