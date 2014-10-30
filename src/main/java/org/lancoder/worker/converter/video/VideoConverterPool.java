package org.lancoder.worker.converter.video;

import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.worker.ConverterListener;
import org.lancoder.worker.WorkerConfig;

public class VideoConverterPool extends Pool<ClientVideoTask> {

	private WorkerConfig config;
	private ConverterListener listener;

	public VideoConverterPool(int threads, ConverterListener listener, WorkerConfig config) {
		super(threads, false);
		this.listener = listener;
		this.config = config;
	}

	@Override
	protected Pooler<ClientVideoTask> getPoolerInstance() {
		return new VideoWorkThread(listener, this.config.getAbsoluteSharedFolder(),
				this.config.getTempEncodingFolder(), config);
	}

}
