package org.lancoder.muxer;

import org.lancoder.common.job.Job;
import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;

public class MuxerPool extends Pool<Job> {

	private String sharedFolder;
	private MuxerListener listener;

	public MuxerPool(MuxerListener listener, String sharedFolder) {
		super(1);
		this.sharedFolder = sharedFolder;
		this.listener = listener;
	}

	@Override
	protected Pooler<Job> getPoolerInstance() {
		return new Muxer(this.listener, sharedFolder);
	}

}
