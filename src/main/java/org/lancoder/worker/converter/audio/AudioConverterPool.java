package org.lancoder.worker.converter.audio;

import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.worker.converter.ConverterListener;
import org.lancoder.worker.converter.ConverterPool;

public class AudioConverterPool extends ConverterPool {

	public AudioConverterPool(int threads, ConverterListener listener) {
		super(threads, listener);
	}

	@Override
	protected boolean hasFree() {
		int size = converters.size();
		return size < threads ? true : false;
	}

	@Override
	public synchronized boolean encode(ClientTask task) {
		if (!(task instanceof ClientAudioTask) || !hasFree()) {
			return false;
		}
		ClientAudioTask aTask = (ClientAudioTask) task;
		AudioWorkThread converter = new AudioWorkThread(aTask, this);
		converters.put(task, converter);
		Thread t = new Thread(converter);
		t.start();
		return true;
	}
}
