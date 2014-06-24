package main.java.drfoliberg.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import main.java.drfoliberg.common.Service;
import main.java.drfoliberg.common.job.RateControlType;
import main.java.drfoliberg.common.task.audio.AudioEncodingTask;

public class AudioConverter extends Service implements ConverterListener {

	AudioEncodingTask task;
	ArrayList<ConverterListener> listeners;
	Process p;

	public AudioConverter(AudioEncodingTask task, ConverterListener listener) {
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
			convertionStarted(task);
			p = pb.start();
			p.waitFor();
			success = p.exitValue() == 0 ? true : false;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (success) {
				convertionFinished(task);
			} else {
				convertionFailed(task);
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
	public synchronized void convertionFinished(AudioEncodingTask t) {
		for (ConverterListener listener : listeners) {
			listener.convertionFinished(t);
		}
	}

	@Override
	public synchronized void convertionStarted(AudioEncodingTask t) {
		for (ConverterListener listener : listeners) {
			listener.convertionStarted(t);
		}
	}

	@Override
	public synchronized void convertionFailed(AudioEncodingTask t) {
		for (ConverterListener listener : listeners) {
			listener.convertionFailed(t);
		}
	}

	public void addListener(ConverterListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ConverterListener listener) {
		listeners.remove(listener);
	}
}
