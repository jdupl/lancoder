package org.lancoder.worker.converter;

import java.util.Hashtable;

import org.lancoder.common.RunnableService;
import org.lancoder.common.Service;
import org.lancoder.common.network.Cause;
import org.lancoder.common.task.ClientTask;
import org.lancoder.worker.WorkerConfig;

public abstract class ConverterPool extends Service implements ConverterListener {

	protected Hashtable<ClientTask, RunnableService> converters;
	protected int threads;
	protected ConverterListener parentListener;

	public ConverterPool(int threads, ConverterListener listener) {
		this.threads = threads;
		this.parentListener = listener;
		converters = new Hashtable<>(threads);
	}

	protected abstract boolean hasFree();

	public abstract boolean encode(ClientTask task);

	public synchronized boolean hasFreeConverters() {
		return hasFree();
	}

	public void workCompleted(ClientTask t) {
		parentListener.workCompleted(t);
		this.converters.remove(t);
	}

	public void workFailed(ClientTask t) {
		parentListener.workFailed(t);
		this.converters.remove(t);
	}

	public void workStarted(ClientTask task) {
		parentListener.workStarted(task);
	}

	public WorkerConfig getConfig() {
		return parentListener.getConfig();
	}

	public synchronized int getThreads() {
		return threads;
	}

	@Override
	public void nodeCrash(Cause cause) {
		// TODO Auto-generated method stub
	}

	public void stop() {
		for (RunnableService converter : converters.values()) {
			converter.stop();
		}
	}
}
