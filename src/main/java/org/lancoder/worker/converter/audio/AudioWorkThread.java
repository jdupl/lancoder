package org.lancoder.worker.converter.audio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.lancoder.common.FilePathManager;
import org.lancoder.common.exceptions.MissingThirdPartyException;
import org.lancoder.common.file_components.streams.AudioStream;
import org.lancoder.common.job.RateControlType;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.common.utils.TimeUtils;
import org.lancoder.ffmpeg.FFmpegReader;
import org.lancoder.worker.converter.Converter;
import org.lancoder.worker.converter.ConverterListener;

public class AudioWorkThread extends Converter<ClientAudioTask> {

	private static Pattern timePattern = Pattern.compile("time=([0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{2,3})");
	private FFmpegReader ffMpegWrapper = new FFmpegReader();

	public AudioWorkThread(ConverterListener listener, FilePathManager filePathManager, FFmpeg ffMpeg) {
		super(listener, filePathManager, ffMpeg);
	}

	private ArrayList<String> getArgs(ClientAudioTask task) {
		ArrayList<String> args = new ArrayList<>();
		AudioStream outStream = task.getStreamConfig().getOutStream();
		AudioStream inStream = task.getStreamConfig().getOrignalStream();

		String absoluteInput = filePathManager.getSharedSourceFile(task).getPath();

		String streamMapping = String.format("0:%d", inStream.getIndex());
		String channelDisposition = String.valueOf(outStream.getChannels().getCount());
		String sampleRate = String.valueOf(outStream.getSampleRate());

		String[] baseArgs = new String[] { ffMpeg.getPath(), "-i", absoluteInput, "-vn", "-sn", "-map", streamMapping,
				"-ac", channelDisposition, "-ar", sampleRate, "-c:a", outStream.getCodec().getEncoder() };
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
		args.add(filePathManager.getLocalTempFile(task).getPath());
		return args;
	}

	private boolean moveFile() {
		try {
			FileUtils.moveFile(filePathManager.getLocalTempFile(task), filePathManager.getSharedFinalFile(task));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void stop() {
		super.stop();
		ffMpegWrapper.stop();
	}

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
		// TODO
	}

	@Override
	protected void start() {
		listener.taskStarted(task);
		boolean success = false;
		createDirs();
		ArrayList<String> args = getArgs(task);
		try {
			success = ffMpegWrapper.read(args, this, true) && moveFile();
		} catch (MissingThirdPartyException e) {
			e.printStackTrace();
		} finally {
			this.active = false;
			if (success) {
				listener.taskCompleted(task);
			} else {
				listener.taskFailed(task);
			}
			destroyTempFolder();
		}
	};

	@Override
	public void onMessage(String line) {
		Matcher m = null;
		m = timePattern.matcher(line);
		if (m.find()) {
			task.getProgress().update(TimeUtils.getMsFromString(m.group(1)) / 1000);
		}
	}
}
