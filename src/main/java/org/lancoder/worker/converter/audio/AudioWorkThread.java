package org.lancoder.worker.converter.audio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.lancoder.common.FilePathManager;
import org.lancoder.common.exceptions.MissingThirdPartyException;
import org.lancoder.common.file_components.streams.original.OriginalAudioStream;
import org.lancoder.common.strategies.stream.AudioEncodeStrategy;
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
		AudioEncodeStrategy audioEncodeStrategy = (AudioEncodeStrategy) task.getStreamConfig().getOutStream()
				.getStrategy();
		OriginalAudioStream inStream = task.getStreamConfig().getOrignalStream();

		String absoluteInput = filePathManager.getSharedSourceFile(task).getPath();

		String streamMapping = String.format("0:%d", inStream.getIndex());
		String channelDisposition = String.valueOf(audioEncodeStrategy.getChannels().getCount());
		String sampleRate = String.valueOf(audioEncodeStrategy.getSampleRate());

		String[] baseArgs = new String[] { ffMpeg.getPath(), "-i", absoluteInput, "-vn", "-sn", "-map", streamMapping,
				"-strict", "-2", "-ac", channelDisposition, "-ar", audioEncodeStrategy.getCodec().formatHz(sampleRate),
				"-c:a", audioEncodeStrategy.getCodec().getEncoder() };
		Collections.addAll(args, baseArgs);

		args.addAll(audioEncodeStrategy.getRateControlArgs());
		// Meta-data mapping
		args.add("-map_metadata");
		args.add(String.format("0:s:%d", inStream.getIndex()));
		args.add(filePathManager.getLocalTempFile(task).getPath());
		return args;
	}

	private boolean moveFile() {
		File destination = filePathManager.getSharedFinalFile(task);
		try {
			if (destination.exists()) {
				Logger logger = Logger.getLogger("lancoder");
				logger.warning(String.format("WARNING: Deleting existing file at destination '%s'%n."
						+ "This might be causing a re-encoding loop !",destination.getAbsoluteFile()));

				destination.delete();
			}
			FileUtils.moveFile(filePathManager.getLocalTempFile(task), destination);
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
	protected void start() {
		this.cancelling = false;
		listener.taskStarted(task);
		boolean success = false;
		createDirs();
		ArrayList<String> args = getArgs(task);
		try {
			success = ffMpegWrapper.read(args, this, true) && moveFile();
		} catch (MissingThirdPartyException e) {
			e.printStackTrace();
		} finally {
			destroyTempFolder();
			if (success) {
				listener.taskCompleted(task);
			} else if (cancelling) {
				listener.taskCancelled(task);
			} else {
				listener.taskFailed(task);
			}
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

	@Override
	public void cancelTask(Object task) {
		this.cancelling = true;
		if (this.task != null && this.task.equals(task)) {
			this.ffMpegWrapper.stop();
		}
	}
}
