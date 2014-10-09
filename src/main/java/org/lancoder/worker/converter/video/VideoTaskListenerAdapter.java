package org.lancoder.worker.converter.video;

import org.lancoder.common.pool.PoolListener;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.video.ClientVideoTask;

public class VideoTaskListenerAdapter implements PoolListener<ClientVideoTask> {
	PoolListener<ClientTask> listener;

	public VideoTaskListenerAdapter(PoolListener<ClientTask> listener) {
		this.listener = listener;
	}

	@Override
	public void started(ClientVideoTask e) {
		this.listener.started(e);
	}

	@Override
	public void completed(ClientVideoTask e) {
		this.listener.completed(e);
	}

	@Override
	public void failed(ClientVideoTask e) {
		this.listener.failed(e);
	}

	@Override
	public void crash(Exception e) {
		this.listener.crash(e);
	}
}
