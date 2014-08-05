package drfoliberg.worker.converter;

import java.util.Hashtable;

import drfoliberg.common.Service;
import drfoliberg.common.task.Task;
import drfoliberg.worker.converter.video.WorkThreadListener;

public abstract class ConverterPool implements WorkThreadListener {
	protected Hashtable<Task, Service> converters;
	protected int threads;

	public ConverterPool(int threads) {
		this.threads = threads;
		converters = new Hashtable<>(threads);
	}

	protected abstract boolean hasFree();

	public abstract boolean encode(Task task, WorkThreadListener listener);

	public synchronized boolean hasFreeConverters() {
		return hasFree();
	}

	public void workCompleted(Task t) {
		this.converters.remove(t);
	}

	public void workFailed(Task t) {
		this.converters.remove(t);
	}

	public synchronized int getThreads() {
		return threads;
	}
}
