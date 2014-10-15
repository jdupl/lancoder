package org.lancoder.worker.converter.audio;

import org.lancoder.common.pool.PoolListener;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.audio.ClientAudioTask;

public class AudioTaskListenerAdapter implements PoolListener<ClientAudioTask> {

	PoolListener<ClientTask> listener;

	public AudioTaskListenerAdapter(PoolListener<ClientTask> listener) {
		this.listener = listener;
	}

	@Override
	public void started(ClientAudioTask e) {
		this.listener.started(e);
	}

	@Override
	public void completed(ClientAudioTask e) {
		this.listener.completed(e);
	}

	@Override
	public void failed(ClientAudioTask e) {
		this.listener.failed(e);
	}

	@Override
	public void crash(Exception e) {
		this.listener.crash(e);
	}

}
