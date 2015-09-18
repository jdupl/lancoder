package org.lancoder.master;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.io.FilenameUtils;
import org.lancoder.common.RunnableServiceAdapter;
import org.lancoder.common.codecs.ChannelDisposition;
import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.codecs.CodecLoader;
import org.lancoder.common.codecs.base.AudioCodec;
import org.lancoder.common.codecs.base.VideoCodec;
import org.lancoder.common.file_components.FileInfo;
import org.lancoder.common.file_components.streams.AudioStream;
import org.lancoder.common.file_components.streams.VideoStream;
import org.lancoder.common.file_components.streams.original.OriginalAudioStream;
import org.lancoder.common.file_components.streams.original.OriginalVideoStream;
import org.lancoder.common.job.FFmpegPreset;
import org.lancoder.common.job.Job;
import org.lancoder.common.job.RateControlType;
import org.lancoder.common.network.messages.web.ApiJobRequest;
import org.lancoder.common.strategies.stream.AudioEncodeStrategy;
import org.lancoder.common.strategies.stream.CopyStrategy;
import org.lancoder.common.strategies.stream.StreamHandlingStrategy;
import org.lancoder.common.strategies.stream.VideoEncodeStrategy;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.StreamConfig;
import org.lancoder.common.task.audio.AudioStreamConfig;
import org.lancoder.common.task.video.VideoStreamConfig;
import org.lancoder.common.third_parties.FFprobe;
import org.lancoder.common.utils.FileUtils;
import org.lancoder.ffmpeg.FFmpegWrapper;

public class JobInitiator extends RunnableServiceAdapter {

	private final static String[] EXTENSIONS = new String[] { "mkv", "mp4", "avi", "mov", "flac", "mp3" };

	private final LinkedBlockingDeque<ApiJobRequest> requests = new LinkedBlockingDeque<>();
	private JobInitiatorListener listener;
	private MasterConfig config;

	public JobInitiator(JobInitiatorListener listener, MasterConfig config) {
		this.listener = listener;
		this.config = config;
	}

	public boolean process(ApiJobRequest request) {
		boolean success = false;

		if (new File(config.getAbsoluteSharedFolder(), request.getInputFile()).exists()) {
			success = true;
			this.requests.add(request);
		}
		return success;
	}

	private Job createJob(ApiJobRequest req, String jobName, File sourceFile, File outputFolder) {
		// Get meta-data from source file
		File absoluteFile = FileUtils.getFile(config.getAbsoluteSharedFolder(), sourceFile.getPath());
		FileInfo fileInfo = FFmpegWrapper.getFileInfo(absoluteFile, sourceFile.getPath(), new FFprobe(config));

		FFmpegPreset preset = req.getPreset();
		RateControlType videoRateControlType = req.getRateControlType();
		CodecEnum videoCodecEnum = req.getVideoCodec();
		VideoCodec videoCodec = (VideoCodec) CodecLoader.fromCodec(videoCodecEnum);
		double requestFrameRate = 0;
		int width = 0;
		int height = 0;
		int videoRate = req.getRate();

		ArrayList<String> extraEncoderArgs = new ArrayList<>();

		// Limit to max pass from the rate control
		int passes = (req.getPasses() <= videoRateControlType.getMaxPass() ? req.getPasses() : videoRateControlType
				.getMaxPass());
		if (passes <= 0) {
			passes = 1;
		}
		int lengthOfTasks = 1000 * 60 * 5; // TODO get length of task (maybe in an 'advanced section')

		// Audio parameters
		RateControlType audioRCT = null;
		int audioRate = 0;
		int audioSampleRate = 0;
		CodecEnum audioCodecEnum = null;
		ChannelDisposition audioChannels = null;
		StreamHandlingStrategy strategy = null;

		switch (req.getAudioPreset()) {
		case COPY:
			strategy = new CopyStrategy();
			break;
		case AUTO:
			// Set default values
			audioRCT = RateControlType.CRF;
			audioRate = 5;
			audioSampleRate = 48000;
			audioCodecEnum = CodecEnum.VORBIS;
			audioChannels = ChannelDisposition.STEREO;
		case MANUAL:
			// Set values from user's request
			audioRCT = req.getAudioRateControlType();
			audioCodecEnum = req.getAudioCodec();
			if (audioCodecEnum == CodecEnum.UNKNOWN) {
				throw new IllegalArgumentException("audio codec is unknown");
			}
			audioChannels = req.getAudioChannels();
			audioSampleRate = req.getAudioSampleRate();
			audioRate = req.getAudioRate();
		default:
			AudioCodec audioCodec = (AudioCodec) CodecLoader.fromCodec(audioCodecEnum);
			strategy = new AudioEncodeStrategy(audioCodec, audioRCT, audioRate, audioChannels, audioSampleRate);
		}

		String fileExtension = "mkv";
		if (audioCodecEnum != null && fileInfo.getVideoStreams().size() == 0 && fileInfo.getAudioStreams().size() == 1) {
			fileExtension = audioCodecEnum.getContainer();
		}

		String outputFileName = String.format("%s.%s", FilenameUtils.getBaseName(sourceFile.getPath()), fileExtension);
		Job job = new Job(jobName, sourceFile.getPath(), lengthOfTasks, fileInfo, outputFolder, outputFileName);

		for (OriginalVideoStream originalStream : fileInfo.getVideoStreams()) {
			double frameRate = requestFrameRate < 1 ? originalStream.getFrameRate() : requestFrameRate;
			VideoEncodeStrategy videoEncodeStrategy = new VideoEncodeStrategy(videoCodec, videoRateControlType,
					videoRate, frameRate, preset, width, height, passes);

			VideoStream streamToEncode = new VideoStream(videoEncodeStrategy, originalStream, originalStream.getIndex());

			VideoStreamConfig streamConfig = new VideoStreamConfig(job.getJobId(), extraEncoderArgs, passes, originalStream,
					streamToEncode);

			job.addStream(streamToEncode, createTasks(streamConfig, job));
		}

		for (OriginalAudioStream originalStream : fileInfo.getAudioStreams()) {
			AudioStream streamToEncode = new AudioStream(strategy, originalStream, originalStream.getIndex());
			AudioStreamConfig config = new AudioStreamConfig(job.getJobId(), extraEncoderArgs, originalStream,
					streamToEncode);
			job.addStream(streamToEncode, createTasks(config, job));
			// TODO Sanitize channel disposition (upmix protection)
			// if (stream.getChannels().getCount() < defaultAudio.getChannels().getCount())
		}

		return job;
	}

	private Job createJob(ApiJobRequest req, String jobName, File sourceFile) {
		File output = FileUtils.getFile(config.getFinalEncodingFolder(), jobName);
		return createJob(req, jobName, sourceFile, output);
	}

	private ArrayList<ClientTask> createTasks(StreamConfig config, Job job) {
		StreamHandlingStrategy handlingStrategy = config.getOutStream().getStrategy();
		return handlingStrategy.createTasks(job, config);
	}

	private Job createJob(ApiJobRequest req, File sourcefile) {
		return createJob(req, req.getName(), sourcefile);
	}

	private void processBatchRequest(ApiJobRequest req) {
		File baseSourceFolder = FileUtils.getFile(config.getAbsoluteSharedFolder(), req.getInputFile());
		String globalJobName = req.getName();
		File relGlobalOutput = FileUtils.getFile(config.getFinalEncodingFolder(), globalJobName);
		// clean shared folder if it already exists TODO
		// File sharedParts = new File(config.getFinalEncodingFolder(), "parts");
		// if (sharedParts.exists()) {
		// sharedParts.delete(); // be hard on others
		// }

		// Create all jobs
		Collection<File> toProcess = FileUtils.listFiles(baseSourceFolder, EXTENSIONS, true);
		for (File absoluteFile : toProcess) {
			File relativeJobFile = new File(relativize(absoluteFile));
			String fileName = FilenameUtils.removeExtension(relativeJobFile.getName());
			URI jobOutputUri = baseSourceFolder.toURI().relativize(absoluteFile.getParentFile().toURI());
			File jobOutput = new File(relGlobalOutput, jobOutputUri.getPath());
			String jobName = String.format("%s - %s ", globalJobName, fileName);

			Job job = createJob(req, jobName, relativeJobFile, jobOutput);
			registerJob(job);
		}
	}

	private void registerJob(Job job) {
		prepareFileSystem(job);
		listener.newJob(job);
	}

	/**
	 * Relativize file from shared directory
	 *
	 * @param file
	 *            The absolute file
	 * @return The relative representation of the file
	 */
	private String relativize(File file) {
		URI uri = new File(config.getAbsoluteSharedFolder()).toURI().relativize(file.toURI());
		return uri.getPath();
	}

	private void processJobRequest(ApiJobRequest req) {
		String relativeSourceFile = req.getInputFile();
		File absoluteSourceFile = new File(config.getAbsoluteSharedFolder(), relativeSourceFile);
		if (absoluteSourceFile.isDirectory()) {
			processBatchRequest(req);
		} else {
			Job job = createJob(req, new File(relativeSourceFile));
			registerJob(job);
		}
	}

	private void prepareFileSystem(Job j) {
		// Create base folders
		File absoluteOutput = FileUtils.getFile(config.getAbsoluteSharedFolder(), j.getOutputFolder());

		if (!absoluteOutput.exists()) {
			absoluteOutput.mkdirs();
		}
		FileUtils.givePerms(absoluteOutput, false);

		File absolutePartsOutput = FileUtils.getFile(config.getAbsoluteSharedFolder(), j.getPartsFolderName());
		if (!absolutePartsOutput.exists()) {
			absolutePartsOutput.mkdirs();
		}
		FileUtils.givePerms(absolutePartsOutput, false);
	}

	@Override
	public void run() {
		try {
			while (!close) {
				processJobRequest(requests.take());
			}
		} catch (InterruptedException e) {
		}
	}
}