package org.lancoder.worker.converter.video;

import org.lancoder.common.FilePathManager;
import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.worker.converter.ConverterListener;

public class VideoConverterPool extends Pool<ClientVideoTask> {

	private ConverterListener listener;
	private FilePathManager filePathManager;

	public VideoConverterPool(int threads, ConverterListener listener, FilePathManager filePathManager) {
		super(threads, false);
		this.listener = listener;
		this.filePathManager = filePathManager;
	}

	@Override
	protected Pooler<ClientVideoTask> getPoolerInstance() {
		return new VideoWorkThread(listener, filePathManager);
	}

}
