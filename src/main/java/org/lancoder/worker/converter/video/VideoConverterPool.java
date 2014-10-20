package org.lancoder.worker.converter.video;

import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.PoolListener;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.worker.WorkerConfig;

public class VideoConverterPool extends Pool<ClientVideoTask> {

	private WorkerConfig config;

	public VideoConverterPool(int threads, PoolListener<ClientVideoTask> listener, WorkerConfig config) {
		super(threads, listener, false);
		this.config = config;
	}

	@Override
	protected Pooler<ClientVideoTask> getNewPooler() {
		return new VideoWorkThread(listener, this.config.getAbsoluteSharedFolder(),
				this.config.getTempEncodingFolder(), config);
	}

}
