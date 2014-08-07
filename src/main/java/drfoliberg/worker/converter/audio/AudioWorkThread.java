package drfoliberg.worker.converter.audio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.io.FileUtils;

import drfoliberg.common.Service;
import drfoliberg.common.job.RateControlType;
import drfoliberg.common.task.audio.AudioEncodingTask;
import drfoliberg.worker.converter.ConverterListener;

public class AudioWorkThread extends Service {

	AudioEncodingTask task;
	ConverterListener listener;
	Process p;

	public AudioWorkThread(AudioEncodingTask task, ConverterListener listener) {
		this.task = task;
		this.listener = listener;
	}

	private ArrayList<String> getArgs(AudioEncodingTask task) {
		String absoluteFolder = this.listener.getConfig().getAbsoluteSharedFolder();
		String absoluteInput = FileUtils.getFile(absoluteFolder, task.getInputFile()).getAbsolutePath();
		String absoluteOutput = FileUtils.getFile(absoluteFolder, task.getOutputFile()).getAbsolutePath();

		ArrayList<String> args = new ArrayList<>();
		String[] baseArgs = new String[] { "ffmpeg", "-i", absoluteInput, "-vn", "-sn", "-ac",
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
		args.add(absoluteOutput);
		return args;
	}

	@Override
	public void run() {
		boolean success = false;

		ArrayList<String> args = getArgs(task);
		System.out.println(args.toString());

		ProcessBuilder pb = new ProcessBuilder(args);

		try {
			listener.workStarted(task);
			p = pb.start();
			p.waitFor();
			success = p.exitValue() == 0 ? true : false;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (success) {
				listener.workCompleted(task);
			} else {
				listener.workFailed(task);
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
}
