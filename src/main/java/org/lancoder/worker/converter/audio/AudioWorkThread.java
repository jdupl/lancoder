package org.lancoder.worker.converter.audio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.lancoder.common.file_components.streams.AudioStream;
import org.lancoder.common.job.RateControlType;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.common.utils.TimeUtils;
import org.lancoder.ffmpeg.FFmpegReader;
import org.lancoder.worker.converter.Converter;
import org.lancoder.worker.converter.ConverterListener;

public class AudioWorkThread extends Converter {

	ClientAudioTask task;
	ConverterListener listener;
	Process p;
	File taskFinalFile;
	Pattern timePattern = Pattern.compile("time=([0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{2,3})");

	public AudioWorkThread(ClientAudioTask task, ConverterListener listener) {
		this.task = task;
		this.listener = listener;
		absoluteSharedDir = new File(listener.getConfig().getAbsoluteSharedFolder());
		taskTempOutputFile = FileUtils.getFile(listener.getConfig().getTempEncodingFolder(), task.getTempFile());
		taskTempOutputFolder = new File(taskTempOutputFile.getParent());
		taskFinalFile = FileUtils.getFile(absoluteSharedDir, task.getTempFile());
		taskFinalFolder = new File(taskFinalFile.getParent());
	}

	private ArrayList<String> getArgs(ClientAudioTask task) {
		ArrayList<String> args = new ArrayList<>();
		AudioStream outStream = task.getStreamConfig().getOutStream();
		AudioStream inStream = task.getStreamConfig().getOrignalStream();

		String absoluteInput = FileUtils.getFile(absoluteSharedDir, inStream.getRelativeFile()).getAbsolutePath();
		String streamMapping = String.format("0:%d", inStream.getIndex());
		String channelDisposition = String.valueOf(outStream.getChannels().getCount());
		String sampleRate = String.valueOf(outStream.getSampleRate());

		String[] baseArgs = new String[] { "ffmpeg", "-i", absoluteInput, "-vn", "-sn", "-map", streamMapping, "-ac",
				channelDisposition, "-ar", sampleRate, "-c:a", outStream.getCodec().getEncoder() };
		Collections.addAll(args, baseArgs);
		switch (outStream.getCodec()) {
		case VORBIS:
			String rateControlString = outStream.getRateControlType() == RateControlType.CRF ? "-q:a" : "-b:a";
			String rate = outStream.getRateControlType() == RateControlType.CRF ? String.valueOf(outStream.getRate())
					: String.format("%dk", outStream.getRate());
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
		args.add(String.format("0:s:%d", inStream.getIndex()));
		args.add(taskTempOutputFile.getPath());
		return args;
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
		FFmpegReader ffmpeg = new FFmpegReader();
		if (ffmpeg.read(args, this, true) && moveFile()) {
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

	@Override
	public void onMessage(String line) {
		Matcher m = null;
		m = timePattern.matcher(line);
		if (m.find()) {
			task.getProgress().update(TimeUtils.getMsFromString(m.group(1)) / 1000);
		}
	}
}
