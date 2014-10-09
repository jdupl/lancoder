package org.lancoder.worker.converter.audio;

import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.PoolListener;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.worker.WorkerConfig;

public class AudioConverterPool extends Pool<ClientAudioTask> {

	private WorkerConfig config;

	public AudioConverterPool(int threads, PoolListener<ClientAudioTask> listener, WorkerConfig config) {
		super(threads, listener);
		this.config = config;
	}

	@Override
	public boolean encode(ClientAudioTask task) {
		AudioWorkThread converter = new AudioWorkThread(task, this, config.getAbsoluteSharedFolder(),
				config.getTempEncodingFolder());
		return this.spawn(converter);
	}

}
