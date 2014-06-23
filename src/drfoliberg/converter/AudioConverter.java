package drfoliberg.converter;

import java.util.ArrayList;
import java.util.Collections;

import drfoliberg.common.job.RateControlType;
import drfoliberg.common.task.audio.AudioEncodingTask;

public class AudioConverter implements Runnable {

	AudioEncodingTask task;

	public AudioConverter(AudioEncodingTask task) {
		this.task = task;
	}

	private static ArrayList<String> getArgs(AudioEncodingTask task) {
		ArrayList<String> args = new ArrayList<>();
		String[] baseArgs = new String[] { "ffmpeg", "-i", task.getInputFile(), "-vn", "-sn", "-ac",
				String.valueOf(task.getChannels()), "-ar", String.valueOf(task.getSampleRate()) };
		Collections.addAll(args, baseArgs);
		switch (task.getCodec()) {
		case VORBIS:
			String rateControlString = task.getRateControlType() == RateControlType.CRF ? "-q:a" : "-b:a";
			String rate = task.getRateControlType() == RateControlType.CRF ? String.valueOf(task.getQualityRate())
					: String.format("%dk", task.getQualityRate());
			args.add(rateControlString);
			args.add(rate);
			break;

		default:
			break;
		}
		args.add(task.getOutputFile());

		return null;
	}

	@Override
	public void run() {
		ArrayList<String> args = getArgs(task);
	}

}
