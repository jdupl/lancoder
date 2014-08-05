package drfoliberg.worker.converter.audio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import drfoliberg.common.Service;
import drfoliberg.common.job.RateControlType;
import drfoliberg.common.network.Cause;
import drfoliberg.common.task.Task;
import drfoliberg.common.task.audio.AudioEncodingTask;
import drfoliberg.worker.WorkerConfig;
import drfoliberg.worker.converter.video.WorkThreadListener;

public class AudioWorkThread extends Service implements WorkThreadListener {

	AudioEncodingTask task;
	ArrayList<WorkThreadListener> listeners;
	Process p;

	public AudioWorkThread(AudioEncodingTask task, WorkThreadListener listener) {
		this.task = task;
		listeners = new ArrayList<>();
		listeners.add(listener);
	}

	private static ArrayList<String> getArgs(AudioEncodingTask task) {
		ArrayList<String> args = new ArrayList<>();
		String[] baseArgs = new String[] { "ffmpeg", "-i", task.getInputFile(), "-vn", "-sn", "-ac",
				String.valueOf(task.getChannels()), "-ar", String.valueOf(task.getSampleRate()), "-c:a",
				task.getCodec().getEncoder() };
		Collections.addAll(args, baseArgs);
		switch (task.getCodec()) {
		case VORBIS:
			String rateControlString = task.getRateControlType() == RateControlType.CRF ? "-q:a" : "-b:a";
			String rate = task.getRateControlType() == RateControlType.CRF ? String.valueOf(task.getQualityRate())
					: String.format("%dk", task.getQualityRate());
			args.add(rateControlString);
			args.add(rate);
			break;
		// TODO support other codecs

		default:
			// TODO unknown codec exception
			break;
		}
		args.add(task.getOutputFile());
		return args;
	}

	@Override
	public void run() {
		boolean success = false;

		ArrayList<String> args = getArgs(task);
		System.out.println(args.toString());

		ProcessBuilder pb = new ProcessBuilder(args);

		try {
//			convertionStarted(task);
			workStarted(task);
			p = pb.start();
			p.waitFor();
			success = p.exitValue() == 0 ? true : false;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (success) {
				workCompleted(task);
			} else {
				workFailed(task);
			}
		}
	}

	@Override
	public void stop() {
		super.stop();
		if (p != null) {
			p.destroy();
		}
	}

	@Override
	public synchronized void workCompleted(Task t) {
		for (WorkThreadListener listener : listeners) {
			listener.workCompleted(t);
		}
	}
	
	public synchronized void workFailed(Task t) {
		for (WorkThreadListener listener : listeners) {
			listener.workFailed(t);
		}
	}

	public void addListener(WorkThreadListener listener) {
		listeners.add(listener);
	}

	public void removeListener(WorkThreadListener listener) {
		listeners.remove(listener);
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
	public void workStarted(Task task) {
		// TODO Auto-generated method stub
		
	}
}
