package org.lancoder.worker.converter;

import java.util.Hashtable;

import org.lancoder.common.Service;
import org.lancoder.common.network.Cause;
import org.lancoder.common.task.ClientTask;
import org.lancoder.worker.WorkerConfig;

public abstract class ConverterPool extends Service implements ConverterListener {

	protected Hashtable<ClientTask, Converter> converters;
	protected int threadCount;
	protected ConverterListener parentListener;
	protected ThreadGroup threadGroup = new ThreadGroup("converter_threads");

	public ConverterPool(int threads, ConverterListener listener) {
		this.threadCount = threads;
		this.parentListener = listener;
		converters = new Hashtable<>(threads);
	}

	protected abstract boolean hasFree();

	public abstract boolean encode(ClientTask task);

	public synchronized boolean hasFreeConverters() {
		return hasFree();
	}

	protected synchronized boolean spawn(Converter converter) {
		if (!hasFree()) {
			return false;
		}
		converters.put(converter.getClientTask(), converter);
		Thread t = new Thread(threadGroup, converter);
		t.start();
		return true;
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
		return threadCount;
	}

	@Override
	public void nodeCrash(Cause cause) {
		// TODO Auto-generated method stub
	}

	public void stop() {
		for (Converter converter : converters.values()) {
			converter.stop();
		}
		threadGroup.interrupt();
	}
}
