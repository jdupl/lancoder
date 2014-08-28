package org.lancoder.master;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.io.FilenameUtils;
import org.lancoder.common.FFmpegProber;
import org.lancoder.common.RunnableService;
import org.lancoder.common.codecs.ChannelDisposition;
import org.lancoder.common.codecs.Codec;
import org.lancoder.common.file_components.FileInfo;
import org.lancoder.common.file_components.streams.AudioStream;
import org.lancoder.common.file_components.streams.VideoStream;
import org.lancoder.common.job.FFmpegPreset;
import org.lancoder.common.job.Job;
import org.lancoder.common.job.RateControlType;
import org.lancoder.common.network.messages.api.ApiJobRequest;
import org.lancoder.common.progress.Unit;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.common.task.video.VideoStreamConfig;
import org.lancoder.common.task.video.VideoTask;
import org.lancoder.common.utils.FileUtils;

public class JobInitiator extends RunnableService {

	private LinkedBlockingDeque<ApiJobRequest> requests = new LinkedBlockingDeque<>();
	private JobInitiatorListener listener;
	private MasterConfig config;

	public JobInitiator(JobInitiatorListener listener, MasterConfig config) {
		this.listener = listener;
		this.config = config;
	}

	public void process(ApiJobRequest request) {
		this.requests.add(request);
	}

	private void createJob(ApiJobRequest req, File sourceFile, String jobName) {
		// Get meta-data from source file
		File absoluteFile = FileUtils.getFile(config.getAbsoluteSharedFolder(), sourceFile.getPath());
		FileInfo fileInfo = FFmpegProber.getFileInfo(absoluteFile, sourceFile.getPath());

		FFmpegPreset preset = req.getPreset();
		RateControlType videoRateControlType = req.getRateControlType();
		Codec videoCodec = Codec.H264;
		double requestFrameRate = 0;
		int width = 0;
		int height = 0;

		ArrayList<String> extraEncoderArgs = new ArrayList<>();

		// Limit to max pass from the rate control
		int passes = (req.getPasses() <= videoRateControlType.getMaxPass() ? req.getPasses() : videoRateControlType
				.getMaxPass());
		if (passes <= 0) {
			passes = 1;
		}
		int lengthOfTasks = 1000 * 60 * 5; // TODO get length of task (maybe in an 'advanced section')
		ArrayList<String> extraArgs = new ArrayList<>(); // TODO get extra encoder args from api request

		// Audio parameters
		RateControlType audioRCT = req.getAudioRateControlType();
		Codec audioCodec = req.getAudioCodec();
		ChannelDisposition audioChannels = req.getAudioChannels();
		int audioSampleRate = req.getAudioSampleRate();
		int audioRate = req.getRate();

		if (audioRCT == RateControlType.AUTO) {
			audioRCT = RateControlType.CRF;
			audioRate = 5;
			audioSampleRate = 48000;
			audioCodec = Codec.VORBIS;
			audioChannels = ChannelDisposition.STEREO;
		}

		Job job = new Job(jobName, sourceFile.getPath(), lengthOfTasks, config.getFinalEncodingFolder(), fileInfo);

		for (VideoStream stream : fileInfo.getVideoStreams()) {
			double frameRate = requestFrameRate < 1 ? stream.getFrameRate() : requestFrameRate;
			VideoStream streamToEncode = new VideoStream(stream.getIndex(), videoCodec, frameRate,
					req.getRate(), videoRateControlType, preset, width, height, fileInfo.getDuration(), Unit.SECONDS,
					req.getPasses());

			VideoStreamConfig config = new VideoStreamConfig(job.getJobId(), extraEncoderArgs, passes, stream,
					streamToEncode);
			// TODO Check width and frame rate

			ArrayList<ClientVideoTask> clientVideoTasks = readStream(config, job);
			ArrayList<VideoTask> tasks = new ArrayList<>();
			for (ClientVideoTask clientTask : clientVideoTasks) {
				tasks.add(clientTask.getTask());
			}
			// TODO send client video tasks to master (to job)

		}

		for (AudioStream stream : fileInfo.getAudioStreams()) {
			// Sanitize channel disposition (upmix protection)
			// if (stream.getChannels().getCount() < defaultAudio.getChannels().getCount()){
			//
			// }
		}

		prepareFileSystem(job);
		listener.newJob(job);
	}

	/**
	 * Create tasks of the stream
	 * 
	 * @param config
	 * @return
	 */
	private ArrayList<ClientVideoTask> readStream(VideoStreamConfig config, Job job) {
		ArrayList<ClientVideoTask> tasks = new ArrayList<>();

		VideoStream outStream = config.getOutStream();
		VideoStream inStream = config.getOrignalStream();
		// exclude copy streams from task creation
		if (outStream.getCodec() != Codec.COPY) {
			long remaining = 0;
			if (inStream.getUnit() == Unit.FRAMES) {
				remaining = (long) (inStream.getUnitCount() / inStream.getFrameRate());
			} else if (inStream.getUnit() == Unit.SECONDS) {
				// convert from ms to seconds
				remaining = inStream.getUnitCount() * 1000;
			}
			long currentMs = 0;
			File relativeTasksOutput = FileUtils.getFile(job.getOutputFolder(), job.getPartsFolderName());
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
				int taskId = job.getTaskCount();
				File relativeTaskOutputFile = null;
				relativeTaskOutputFile = FileUtils.getFile(relativeTasksOutput,
						String.format("part-%d.mpeg.ts", taskId)); // TODO get extension from codec
				long ms = end - start;
				long unitCount = (long) Math.floor((ms / 1000 * outStream.getFrameRate()));

				VideoTask task = new VideoTask(taskId, job.getJobId(), outStream.getStepCount(), start, end, unitCount,
						Unit.FRAMES);

				ClientVideoTask clientVideoTask = new ClientVideoTask(task, config, relativeTaskOutputFile.getPath());
				tasks.add(clientVideoTask);
			}
		}
		return tasks;

		// Get relative (to absolute shared directory) output folder for this job's tasks

	}

	private void createJob(ApiJobRequest req, File sourcefile) {
		createJob(req, sourcefile, req.getName());
	}

	private void processBatchRequest(ApiJobRequest req) {
		System.out.println("Directory given");
		File absoluteFolder = new File(new File(config.getAbsoluteSharedFolder()), req.getInputFile());
		Collection<File> toProcess = FileUtils.listFiles(absoluteFolder, new String[] { "mkv", "mp4", "avi", "mov" },
				true);
		for (File absoluteFile : toProcess) {
			String relativePath = new File(config.getAbsoluteSharedFolder()).toURI().relativize(absoluteFile.toURI())
					.getPath();
			File relativeFile = new File(relativePath);
			String fileName = FilenameUtils.removeExtension(relativeFile.getName());
			String jobName = String.format("%s - %s", req.getName(), fileName);
			createJob(req, relativeFile, jobName);
		}
	}

	private void processJobRequest(ApiJobRequest req) {
		String relativeSourceFile = req.getInputFile();
		File absoluteSourceFile = new File(config.getAbsoluteSharedFolder(), relativeSourceFile);
		if (absoluteSourceFile.isDirectory()) {
			processBatchRequest(req);
		} else {
			createJob(req, new File(relativeSourceFile));
		}
	}

	private void prepareFileSystem(Job j) {
		// Create base folders
		File absoluteOutput = FileUtils.getFile(config.getAbsoluteSharedFolder(), j.getOutputFolder());
		File absolutePartsOutput = FileUtils.getFile(absoluteOutput, j.getPartsFolderName());
		if (absoluteOutput.exists()) {
			try {
				// Attempt to clean
				System.err.printf("Directory is not empty. Attempting to clean %s\n", absoluteOutput.toString());
				FileUtils.cleanDirectory(absoluteOutput);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			absoluteOutput.mkdirs();
		}
		FileUtils.givePerms(absoluteOutput, false);
		absolutePartsOutput.mkdir();
		FileUtils.givePerms(absolutePartsOutput, false);
	}

	@Override
	public void run() {
		try {
			while (!close) {
				processJobRequest(requests.take());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		// TODO Auto-generated method stub
	}
}
