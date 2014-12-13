package org.lancoder.worker.converter.video;

import org.lancoder.common.FilePathManager;
import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.worker.converter.ConverterListener;

public class VideoConverterPool extends Pool<ClientVideoTask> {

	private ConverterListener listener;
	private FilePathManager filePathManager;
	private FFmpeg ffMpeg;

	public VideoConverterPool(int threads, ConverterListener listener, FilePathManager filePathManager, FFmpeg ffMpeg) {
		super(threads, false);
		this.listener = listener;
		this.filePathManager = filePathManager;
		this.ffMpeg = ffMpeg;
	}

	@Override
	protected Pooler<ClientVideoTask> getPoolerInstance() {
		return new VideoWorkThread(listener, filePathManager, ffMpeg);
	}

}
