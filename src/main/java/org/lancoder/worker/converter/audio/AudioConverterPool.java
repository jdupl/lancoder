package org.lancoder.worker.converter.audio;

import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.worker.WorkerConfig;
import org.lancoder.worker.converter.ConverterListener;

public class AudioConverterPool extends Pool<ClientAudioTask> {

	private WorkerConfig config;
	private ConverterListener listener;

	public AudioConverterPool(int threads,ConverterListener listener, WorkerConfig config) {
		super(threads, false);
		this.config = config;
		this.listener = listener;
	}

	@Override
	protected Pooler<ClientAudioTask> getPoolerInstance() {
		return new AudioWorkThread(listener, config.getAbsoluteSharedFolder(), config.getTempEncodingFolder(), config);
	}

}
