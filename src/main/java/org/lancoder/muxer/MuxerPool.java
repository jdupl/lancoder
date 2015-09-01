package org.lancoder.muxer;

import org.lancoder.common.FilePathManager;
import org.lancoder.common.job.Job;
import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.PoolWorker;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.common.third_parties.MkvMerge;

public class MuxerPool extends Pool<Job> {

	private FilePathManager filePathManager;
	private MuxerListener listener;
	private FFmpeg ffMpeg;
	private MkvMerge mkvMerge;

	public MuxerPool(MuxerListener listener, FilePathManager filePathManager, FFmpeg ffMpeg, MkvMerge mkvMerge) {
		super(1);
		this.filePathManager = filePathManager;
		this.listener = listener;
		this.ffMpeg = ffMpeg;
		this.mkvMerge = mkvMerge;
	}

	@Override
	protected PoolWorker<Job> getPoolWorkerInstance() {
		return new MuxerWorkerWrapper(ffMpeg, mkvMerge, filePathManager, listener);
	}

}
