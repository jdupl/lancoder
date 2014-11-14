package org.lancoder.muxer;

import org.lancoder.common.FilePathManager;
import org.lancoder.common.job.Job;
import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;

public class MuxerPool extends Pool<Job> {

	private FilePathManager filePathManager;
	private MuxerListener listener;

	public MuxerPool(MuxerListener listener, FilePathManager filePathManager) {
		super(1);
		this.filePathManager = filePathManager;
		this.listener = listener;
	}

	@Override
	protected Pooler<Job> getPoolerInstance() {
		return new Muxer(this.listener, filePathManager);
	}

}
