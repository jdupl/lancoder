package main.java.drfoliberg.converter;

import java.util.Hashtable;

import main.java.drfoliberg.common.status.TaskState;
import main.java.drfoliberg.common.task.audio.AudioEncodingTask;

public class ConverterPool implements ConverterListener {

	private Hashtable<AudioEncodingTask, AudioConverter> converters;
	private int threads;

	public ConverterPool(int threads) {
		this.threads = threads;
		converters = new Hashtable<>(threads);
	}

	public void stop() {
		for (AudioConverter converter : converters.values()) {
			converter.stop();
		}
	}

	public synchronized boolean encode(AudioEncodingTask task, ConverterListener listener) {
		if (!hasFree()) {
			return false;
		}
		task.setTaskState(TaskState.TASK_COMPUTING);
		AudioConverter converter = new AudioConverter(task, listener);
		converter.addListener(this);
		converters.put(task, converter);
		Thread t = new Thread(converter);
		t.start();
		return true;
	}

	private boolean hasFree() {
		int size = converters.size();
		return size < threads ? true : false;
	}

	public synchronized boolean hasFreeConverters() {
		return hasFree();
	}

	@Override
	public void convertionFinished(AudioEncodingTask t) {
		this.converters.remove(t);
	}

	@Override
	public void convertionStarted(AudioEncodingTask t) {
		// lookin' good
	}

	@Override
	public void convertionFailed(AudioEncodingTask t) {
		this.converters.remove(t);
	}

	public synchronized int getThreads() {
		return threads;
	}

	public synchronized void setThreads(int threads) {
		this.threads = threads;
	}

}
