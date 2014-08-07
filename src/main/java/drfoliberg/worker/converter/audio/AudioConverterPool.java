package drfoliberg.worker.converter.audio;

import drfoliberg.common.Service;
import drfoliberg.common.task.Task;
import drfoliberg.common.task.audio.AudioEncodingTask;
import drfoliberg.worker.converter.ConverterPool;
import drfoliberg.worker.converter.ConverterListener;

public class AudioConverterPool extends ConverterPool {

	public AudioConverterPool(int threads, ConverterListener listener) {
		super(threads, listener);
	}

	public void stop() {
		for (Service converter : converters.values()) {
			converter.stop();
		}
	}

	@Override
	protected boolean hasFree() {
		int size = converters.size();
		return size < threads ? true : false;
	}

	@Override
	public synchronized boolean encode(Task task) {
		if (!(task instanceof AudioEncodingTask) || !hasFree()) {
			return false;
		}
		AudioEncodingTask aTask = (AudioEncodingTask) task;
		AudioWorkThread converter = new AudioWorkThread(aTask, this);
		converters.put(task, converter);
		Thread t = new Thread(converter);
		t.start();
		return true;
	}

}
