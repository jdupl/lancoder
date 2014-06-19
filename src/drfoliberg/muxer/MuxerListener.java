package drfoliberg.muxer;

import drfoliberg.common.job.Job;

public interface MuxerListener {

	public void muxingStarting(Job job);

	public void muxingCompleted(Job job);

	public void muxingFailed(Job job, Exception e);

}
