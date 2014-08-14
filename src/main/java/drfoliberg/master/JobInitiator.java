package drfoliberg.master;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.io.FilenameUtils;

import drfoliberg.common.FFmpegProber;
import drfoliberg.common.RunnableService;
import drfoliberg.common.job.FFmpegPreset;
import drfoliberg.common.job.Job;
import drfoliberg.common.job.JobConfig;
import drfoliberg.common.job.RateControlType;
import drfoliberg.common.network.messages.api.ApiJobRequest;
import drfoliberg.common.task.audio.AudioCodec;
import drfoliberg.common.task.audio.AudioEncodingTask;
import drfoliberg.common.utils.FileUtils;

public class JobInitiator extends RunnableService {

	private LinkedBlockingDeque<ApiJobRequest> requests = new LinkedBlockingDeque<>();
	private JobInitiatorListener listener;
	private MasterConfig config;

	public JobInitiator(JobInitiatorListener listener, MasterConfig config) {
		this.listener = listener;
		this.config = config;
	}
	
	public void process(ApiJobRequest request){
		this.requests.add(request);
	}

	private void createJob(ApiJobRequest req, File sourceFile, String jobName) {
		System.out.println("Creating job for file "+ sourceFile); // DEBUG
		String absoluteSourceFile = new File(config.getAbsoluteSharedFolder(), sourceFile.getPath()).getAbsolutePath();
		// Get meta-data from source file
		long lengthOfJob = (long) (FFmpegProber.getSecondsDuration(absoluteSourceFile) * 1000);
		float frameRate = FFmpegProber.getFrameRate(absoluteSourceFile);
		int frameCount = (int) Math.floor((lengthOfJob / 1000 * frameRate));

		FFmpegPreset preset = req.getPreset();
		RateControlType rateControlType = req.getRateControlType();

		// Limit to max pass from the rate control
		int passes = (req.getPasses() <= rateControlType.getMaxPass() ? req.getPasses() : rateControlType.getMaxPass());
		if (passes <= 0) {
			passes = 1;
		}
		int lengthOfTasks = 1000 * 60 * 5; // TODO get length of task (maybe in an 'advanced section')
		ArrayList<String> extraArgs = new ArrayList<>(); // TODO get extra encoder args from api request

		JobConfig conf = new JobConfig(sourceFile.getPath(), rateControlType, req.getRate(), passes, preset, extraArgs);

		// TODO Move to factories (audio task, video task)
		Job job = new Job(conf, jobName, lengthOfTasks, lengthOfJob, frameCount, frameRate,
				config.getFinalEncodingFolder());
		// Create audio tasks
		int nextTaskId = job.getTasks().size();
		File output = FileUtils.getFile(job.getOutputFolder(), String.valueOf(nextTaskId));
		job.getAudioTasks().add(
				new AudioEncodingTask(AudioCodec.VORBIS, 2, 44100, 3, RateControlType.CRF, conf.getSourceFile(), output
						.getPath(), job.getJobId(), nextTaskId));

		prepareFileSystem(job);

		listener.newJob(job);
	}

	private void createJob(ApiJobRequest req, File sourcefile) {
		createJob(req, sourcefile, req.getName());
	}

	private void processBatchRequest(ApiJobRequest req) {
		System.out.println("Directory given");
		File absoluteFolder = new File(new File(config.getAbsoluteSharedFolder()), req.getInputFile());
		Collection<File> toProcess = FileUtils.listFiles(absoluteFolder, new String[] { "mkv", "mp4", "avi" }, true);
		for (File file : toProcess) {
			String fileName = FilenameUtils.removeExtension(file.getName());
			String jobName = String.format("%s - %s", req.getName(), fileName);
			createJob(req, file, jobName);
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
