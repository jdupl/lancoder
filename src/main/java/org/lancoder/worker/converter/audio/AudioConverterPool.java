package org.lancoder.worker.converter.audio;

import org.lancoder.common.FilePathManager;
import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.worker.converter.ConverterListener;

public class AudioConverterPool extends Pool<ClientAudioTask> {

	private ConverterListener listener;
	private FilePathManager filePathManager;

	public AudioConverterPool(int threads, ConverterListener listener, FilePathManager filePathManager) {
		super(threads, false);
		this.listener = listener;
		this.filePathManager = filePathManager;
	}

	@Override
	protected Pooler<ClientAudioTask> getPoolerInstance() {
		return new AudioWorkThread(listener, filePathManager);
	}

}
