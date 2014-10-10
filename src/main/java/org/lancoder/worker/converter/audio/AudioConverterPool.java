package org.lancoder.worker.converter.audio;

import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.PoolListener;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.worker.WorkerConfig;

public class AudioConverterPool extends Pool<ClientAudioTask> {

	private WorkerConfig config;

	public AudioConverterPool(int threads, PoolListener<ClientAudioTask> listener, WorkerConfig config) {
		super(threads, listener);
		this.config = config;
	}

	@Override
	protected Pooler<ClientAudioTask> getNewPooler() {
		AudioWorkThread converter = new AudioWorkThread(this, config.getAbsoluteSharedFolder(),
				config.getTempEncodingFolder());
		return null;
	}

}
