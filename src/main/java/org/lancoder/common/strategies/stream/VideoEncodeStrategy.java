package org.lancoder.common.strategies.stream;

import java.io.File;
import java.util.ArrayList;

import org.lancoder.common.codecs.base.AbstractCodec;
import org.lancoder.common.file_components.streams.original.OriginalVideoStream;
import org.lancoder.common.job.FFmpegPreset;
import org.lancoder.common.job.Job;
import org.lancoder.common.job.RateControlType;
import org.lancoder.common.progress.Unit;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.StreamConfig;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.common.task.video.VideoStreamConfig;
import org.lancoder.common.task.video.VideoTask;
import org.lancoder.common.utils.FileUtils;

public class VideoEncodeStrategy extends EncodeStrategy {

	private static final long serialVersionUID = 4179288217629677026L;
	private double frameRate = 0;
	private FFmpegPreset preset = FFmpegPreset.MEDIUM;
	private int width = 0;
	private int height = 0;
	private int stepCount = 1;

	public VideoEncodeStrategy(AbstractCodec codec, RateControlType rateControlType, int rate, double frameRate,
			FFmpegPreset preset, int width, int height, int stepCount) {
		super(codec, rateControlType, rate);
		this.frameRate = frameRate;
		this.preset = preset;
		this.width = width;
		this.height = height;
		this.stepCount = stepCount;
	}

	public double getFrameRate() {
		return frameRate;
	}

	public FFmpegPreset getPreset() {
		return preset;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getStepCount() {
		return stepCount;
	}

	@Override
	public ArrayList<ClientTask> createTasks(Job job, StreamConfig config) {
		VideoStreamConfig streamConfig = (VideoStreamConfig) config;
		OriginalVideoStream inStream = streamConfig.getOrignalStream();
		ArrayList<ClientTask> tasks = new ArrayList<>();
		int taskId = job.getTaskCount();
		// exclude copy streams from task creation
		long remaining = 0;
		if (inStream.getUnit() == Unit.FRAMES) {
			remaining = (long) (inStream.getUnitCount() / inStream.getFrameRate());
		} else if (inStream.getUnit() == Unit.SECONDS) {
			// convert from ms to seconds
			remaining = inStream.getUnitCount() * 1000;
		}
		long currentMs = 0;
		File relativeTasksOutput = FileUtils.getFile(job.getPartsFolderName());
		while (remaining > 0) {
			long start = currentMs;
			long end = 0;
			if ((((double) remaining - job.getLengthOfTasks()) / job.getLengthOfJob()) <= 0.15) {
				end = job.getLengthOfJob();
				remaining = 0;
			} else {
				end = currentMs + job.getLengthOfTasks();
				remaining -= job.getLengthOfTasks();
				currentMs += job.getLengthOfTasks();
			}
			String extension = getCodec().needsTranscode() ? "mpeg.ts" : getCodec().getContainer();
			File relativeTaskOutputFile = FileUtils.getFile(relativeTasksOutput,
					String.format("part-%d.%s", taskId, extension));
			long ms = end - start;
			long unitCount = (long) Math.floor((ms / 1000 * getFrameRate()));
			VideoTask task = new VideoTask(taskId, job.getJobId(), getStepCount(), start, end, unitCount, Unit.FRAMES,
					relativeTaskOutputFile.getPath());
			tasks.add(new ClientVideoTask(task, streamConfig));
			taskId++;
		}
		return tasks;
	}

}
