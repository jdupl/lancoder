package drfoliberg.worker.converter.audio;

import drfoliberg.common.Service;
import drfoliberg.common.network.Cause;
import drfoliberg.common.status.TaskState;
import drfoliberg.common.task.Task;
import drfoliberg.common.task.audio.AudioEncodingTask;
import drfoliberg.worker.WorkerConfig;
import drfoliberg.worker.converter.ConverterPool;
import drfoliberg.worker.converter.video.WorkThreadListener;

public class AudioConverterPool extends ConverterPool implements WorkThreadListener {

	public AudioConverterPool(int threads) {
		super(threads);
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
	public void workStarted(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void nodeCrash(Cause cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public WorkerConfig getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public synchronized boolean encode(Task task, WorkThreadListener listener) {
		if (!(task instanceof AudioEncodingTask) || !hasFree()) {
			return false;
		}
		AudioEncodingTask aTask = (AudioEncodingTask) task;
		task.setTaskState(TaskState.TASK_COMPUTING);
		AudioWorkThread converter = new AudioWorkThread(aTask, listener);
		converter.addListener(this);
		converters.put(task, converter);
		Thread t = new Thread(converter);
		t.start();
		return true;
	}

}
