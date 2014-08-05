package drfoliberg.worker.converter;

import java.util.Hashtable;

import drfoliberg.common.Service;
import drfoliberg.common.network.Cause;
import drfoliberg.common.task.Task;
import drfoliberg.worker.WorkerConfig;
import drfoliberg.worker.converter.video.WorkThreadListener;

public abstract class ConverterPool implements WorkThreadListener {

	protected Hashtable<Task, Service> converters;
	protected int threads;
	protected WorkThreadListener parentListener;

	public ConverterPool(int threads, WorkThreadListener listener) {
		this.threads = threads;
		this.parentListener = listener;
		converters = new Hashtable<>(threads);
	}

	protected abstract boolean hasFree();

	public abstract boolean encode(Task task);

	public synchronized boolean hasFreeConverters() {
		return hasFree();
	}

	public void workCompleted(Task t) {
		parentListener.workCompleted(t);
		this.converters.remove(t);
	}

	public void workFailed(Task t) {
		parentListener.workFailed(t);
		this.converters.remove(t);
	}

	public void workStarted(Task task) {
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
}
