package org.lancoder.muxer;

import org.lancoder.common.job.Job;

public interface MuxerListener {

	public void jobMuxingStarted(Job e);

	public void jobMuxingCompleted(Job e);

	public void jobMuxingFailed(Job e);

}
