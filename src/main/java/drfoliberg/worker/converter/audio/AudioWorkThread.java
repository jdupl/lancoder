package drfoliberg.worker.converter.audio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import drfoliberg.common.RunnableService;
import drfoliberg.common.job.RateControlType;
import drfoliberg.common.task.audio.AudioEncodingTask;
import drfoliberg.common.utils.TimeUtils;
import drfoliberg.worker.converter.ConverterListener;

public class AudioWorkThread extends RunnableService {

	AudioEncodingTask task;
	ConverterListener listener;
	Process p;

	public AudioWorkThread(AudioEncodingTask task, ConverterListener listener) {
		this.task = task;
		this.listener = listener;
	}

	private ArrayList<String> getArgs(AudioEncodingTask task) {
		String absoluteFolder = this.listener.getConfig().getAbsoluteSharedFolder();
		String absoluteInput = FileUtils.getFile(absoluteFolder, task.getSourceFile()).getAbsolutePath();
		String absoluteOutput = FileUtils.getFile(absoluteFolder, task.getOutputFile()).getAbsolutePath();

		String mapping = String.format("0:%d", task.getStream().getIndex());

		ArrayList<String> args = new ArrayList<>();
		String[] baseArgs = new String[] { "ffmpeg", "-i", absoluteInput, "-vn", "-sn", "-map", mapping, "-ac",
				String.valueOf(task.getChannels()), "-ar", String.valueOf(task.getSampleRate()), "-c:a",
				task.getCodec().getEncoder() };
		Collections.addAll(args, baseArgs);
		switch (task.getCodec()) {
		case VORBIS:
			String rateControlString = task.getRateControlType() == RateControlType.CRF ? "-q:a" : "-b:a";
			String rate = task.getRateControlType() == RateControlType.CRF ? String.valueOf(task.getRate()) : String
					.format("%dk", task.getRate());
			args.add(rateControlString);
			args.add(rate);
			break;
		// TODO support other codecs
		default:
			// TODO unknown codec exception
			break;
		}
		// Meta-data mapping
		args.add("-map_metadata");
		args.add(String.format("0:s:%d", task.getStream().getIndex())); // TODO map to stream insted of global
		args.add(absoluteOutput);
		return args;
	}

	@Override
	public void run() {
		boolean success = false;
		ArrayList<String> args = getArgs(task);
		System.out.println(args.toString()); // DEBUG
		listener.workStarted(task);
		ProcessBuilder pb = new ProcessBuilder(args);
		Scanner s = null;
		try {
			Pattern timePattern = Pattern.compile("time=([0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{2,3})");
			Matcher m = null;
			p = pb.start();
			s = new Scanner(p.getErrorStream());
			while (s.hasNext() && !close) {
				m = timePattern.matcher(s.nextLine());
				if (m.find()) {
					System.out.println(TimeUtils.getMsFromString(m.group(1)));
				}
			}

			success = p.exitValue() == 0 ? true : false;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (close) {
				p.destroy();
			}
			if (s != null) {
				s.close();
			}
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

	@Override
	public void serviceFailure(Exception e) {
		listener.nodeCrash(null);
		// TODO
	}
}
