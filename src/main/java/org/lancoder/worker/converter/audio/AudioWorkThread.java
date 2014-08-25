package org.lancoder.worker.converter.audio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.lancoder.common.job.RateControlType;
import org.lancoder.common.task.audio.AudioEncodingTask;
import org.lancoder.common.utils.TimeUtils;
import org.lancoder.worker.converter.Converter;
import org.lancoder.worker.converter.ConverterListener;

public class AudioWorkThread extends Converter {

	AudioEncodingTask task;
	ConverterListener listener;
	Process p;
	File taskFinalFile;

	public AudioWorkThread(AudioEncodingTask task, ConverterListener listener) {
		this.task = task;
		this.listener = listener;

		absoluteSharedDir = new File(listener.getConfig().getAbsoluteSharedFolder());
		String extension = task.getCodec().getContainer();
		taskTempOutputFolder = FileUtils.getFile(listener.getConfig().getTempEncodingFolder(), task.getJobId(),
				String.valueOf(task.getTaskId()));
		taskTempOutputFile = new File(taskTempOutputFolder, String.format("%d.%s", task.getTaskId(), extension));
		taskFinalFolder = FileUtils.getFile(absoluteSharedDir, task.getOutputFile()).getParentFile();
		taskFinalFile = FileUtils.getFile(absoluteSharedDir, task.getOutputFile());
	}

	private ArrayList<String> getArgs(AudioEncodingTask task) {
		String absoluteInput = FileUtils.getFile(absoluteSharedDir, task.getSourceFile()).getAbsolutePath();

		String streamMapping = String.format("0:%d", task.getStream().getIndex());
		ArrayList<String> args = new ArrayList<>();
		String[] baseArgs = new String[] { "ffmpeg", "-i", absoluteInput, "-vn", "-sn", "-map", streamMapping, "-ac",
				String.valueOf(task.getChannelDisposition().getCount()), "-ar", String.valueOf(task.getSampleRate()), "-c:a",
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
		args.add(taskTempOutputFile.getPath());
		return args;
	}

	private boolean encode(ArrayList<String> args) {
		boolean success = false;
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
					task.update(TimeUtils.getMsFromString(m.group(1)) / 1000);
				}
			}
			success = p.waitFor() == 0 ? true : false;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (close) {
				p.destroy();
			}
			if (s != null) {
				s.close();
			}
		}
		return success;
	}

	private boolean moveFile() {
		try {
			FileUtils.moveFile(taskTempOutputFile, taskFinalFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void run() {
		ArrayList<String> args = getArgs(task);
		System.out.println(args.toString()); // DEBUG
		listener.workStarted(task);
		createDirs();
		if (encode(args) && moveFile()) {
			listener.workCompleted(task);
			cleanTempPart();
		} else {
			listener.workFailed(task);
			cleanTempPart();
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
