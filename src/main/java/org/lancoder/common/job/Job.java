package org.lancoder.common.job;

import java.io.File;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.lancoder.common.codecs.Codec;
import org.lancoder.common.file_components.FileInfo;
import org.lancoder.common.file_components.streams.AudioStream;
import org.lancoder.common.file_components.streams.Stream;
import org.lancoder.common.file_components.streams.VideoStream;
import org.lancoder.common.status.JobState;
import org.lancoder.common.status.TaskState;
import org.lancoder.common.task.Task;
import org.lancoder.common.task.audio.AudioEncodingTask;
import org.lancoder.common.task.audio.AudioTaskConfig;
import org.lancoder.common.task.video.TaskInfo;
import org.lancoder.common.task.video.VideoEncodingTask;
import org.lancoder.common.task.video.VideoTaskConfig;

/**
 * A job is the whole process of taking the source file, splitting it if necessary, encoding it and merge back all
 * tracks. Tasks will be dispatched to nodes by the master.
 * 
 * @author justin
 * 
 */
public class Job implements Comparable<Job>, Serializable {

	private static final long serialVersionUID = -3817299446490049451L;

	private String jobId;
	private String jobName;
	private JobState jobStatus = JobState.JOB_TODO;
	private int lengthOfTasks;
	private long lengthOfJob;
	private int frameCount;
	private double frameRate;
	private int priority;
	/**
	 * Output path of this job, relative to absolute shared directory
	 */
	private String outputFolder;
	/**
	 * Filename for final output file relative to outputFolder
	 */
	private String outputFileName;
	/**
	 * The folder in which to store the parts before muxing
	 */
	private String partsFolderName;
	private FileInfo fileInfo;
	// private JobConfig jobConfig;

	private ArrayList<VideoEncodingTask> videoTasks = new ArrayList<>();
	private ArrayList<AudioEncodingTask> audioTasks = new ArrayList<>();
	private int taskCount = 0;

	public Job(String jobName, String inputFile, int lengthOfTasks, String encodingOutputFolder, FileInfo fileInfo,
			VideoTaskConfig vconfig, AudioTaskConfig aconfig) {
		this.jobName = jobName;
		this.lengthOfTasks = lengthOfTasks;
		this.lengthOfJob = fileInfo.getDuration();
		this.frameRate = fileInfo.getMainVideoStream().getFramerate();
		this.fileInfo = fileInfo;
		this.partsFolderName = "parts"; // TODO Why would this change ? Perhaps move to constant.

		// Estimate the frame count from the frame rate and length
		this.frameCount = (int) Math.floor((lengthOfJob / 1000 * frameRate));
		// Get source's filename
		File source = new File(inputFile);
		// Set output's filename
		this.outputFileName = String.format("%s.mkv", FilenameUtils.removeExtension(source.getName()));
		// Get /sharedFolder/LANcoder/jobsOutput/jobName/ (without the shared folder)
		File relativeEncodingOutput = FileUtils.getFile(encodingOutputFolder, jobName);
		this.outputFolder = relativeEncodingOutput.getPath();

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] byteArray = md.digest((inputFile + jobName + System.currentTimeMillis()).getBytes());
			String result = "";
			for (int i = 0; i < byteArray.length; i++) {
				result += Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1);
			}
			this.jobId = result;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			// even if the algorithm is not available, don't crash
			this.jobId = String.valueOf(System.currentTimeMillis());
		}
		createTasks(aconfig, vconfig);
	}

	/**
	 * Get the tasks associated to this stream.
	 * 
	 * @param stream
	 *            The stream to look for
	 * @return An ArrayList of related tasks
	 */
	public ArrayList<Task> getTasksForStream(Stream stream) {
		ArrayList<Task> tasks = new ArrayList<>();
		for (int i = 0; i < this.getTasks().size(); i++) {
			Task task = this.getTasks().get(i);
			if (task.getStream().equals(stream)) {
				tasks.add(task);
			}
		}
		return tasks;
	}

	/**
	 * Creates tasks of the job with good handling of paths. TODO add subtitles to the job
	 */
	private void createTasks(AudioTaskConfig aconfig, VideoTaskConfig vconfig) {
		for (Stream stream : this.fileInfo.getStreams()) {
			if (stream instanceof VideoStream) {
				this.videoTasks.addAll(createVideoTasks((VideoStream) stream, vconfig));
			} else if (stream instanceof AudioStream) {
				this.audioTasks.add(createAudioTask((AudioStream) stream, aconfig));
			}
		}
	}

	/**
	 * Process a VideoStream and split into multiple VideoEncodingTasks
	 * 
	 * @param stream
	 *            The stream to process
	 * @return The VideoEncodingTasks that will be encoded
	 */
	private ArrayList<VideoEncodingTask> createVideoTasks(VideoStream stream, VideoTaskConfig config) {
		long currentMs = 0;
		ArrayList<VideoEncodingTask> tasks = new ArrayList<>();

		long remaining = fileInfo.getDuration();

		// Get relative (to absolute shared directory) output folder for this job's tasks
		File relativeTasksOutput = FileUtils.getFile(getOutputFolder(), getPartsFolderName());
		while (remaining > 0) {
			long start = currentMs;
			long end = 0;

			if ((((double) remaining - getLengthOfTasks()) / getLengthOfJob()) <= 0.15) {
				end = getLengthOfJob();
				remaining = 0;
			} else {
				end = currentMs + lengthOfTasks;
				remaining -= lengthOfTasks;
				currentMs += lengthOfTasks;
			}
			int taskId = taskCount++;
			File relativeTaskOutputFile = null;
			if (config.getCodec() == Codec.COPY) {
				relativeTaskOutputFile = new File(config.getSourceFile());
			} else {
				relativeTaskOutputFile = FileUtils.getFile(relativeTasksOutput,
						String.format("part-%d.mpeg.ts", taskId)); // TODO get extension from codec
			}
			long ms = end - start;
			long frameCount = (long) Math.floor((ms / 1000 * stream.getFramerate()));
			TaskInfo info = new TaskInfo(taskId, getJobId(), relativeTaskOutputFile.getPath(), start, end, frameCount);
			VideoEncodingTask task = new VideoEncodingTask(info, stream, config);
			tasks.add(task);
		}
		return tasks;
	}

	/**
	 * Process an AudioStream and create an AudioEncodingTask (with hardcoded vorbis settings). TODO handle multiple
	 * audio codec.
	 * 
	 * @param stream
	 *            The stream to encode
	 * @return The AudioEncodingTask
	 */
	private AudioEncodingTask createAudioTask(AudioStream stream, AudioTaskConfig config) {
		int nextTaskId = taskCount++;
		File relativeTasksOutput = FileUtils.getFile(getOutputFolder(), getPartsFolderName());
		File output = FileUtils.getFile(relativeTasksOutput,
				String.format("%d.%s", nextTaskId, config.getCodec().getContainer()));
		TaskInfo info = new TaskInfo(nextTaskId, getJobId(), output.getPath(), 0, fileInfo.getDuration(),
				fileInfo.getDuration() / 1000);
		AudioEncodingTask task = new AudioEncodingTask(info, stream, config);
		return task;
	}

	/**
	 * Returns next task to encode. Changes Job status if job is not started yet.
	 * 
	 * @return The task or null if no task is available
	 */
	public synchronized VideoEncodingTask getNextTask() {
		if (getTaskRemainingCount() == 0) {
			return null;
		}
		if (this.getJobStatus() == JobState.JOB_TODO) {
			// TODO move this to job manager
			this.setJobStatus(JobState.JOB_COMPUTING);
		}
		for (VideoEncodingTask task : this.videoTasks) {
			if (task.getTaskState() == TaskState.TASK_TODO) {
				return task;
			}
		}
		return null;
	}

	/**
	 * Counts if necessary the tasks currently not processed. A task being processed by a node counts as processed.
	 * 
	 * @return The count of tasks left to dispatch
	 */
	public synchronized int getTaskRemainingCount() {
		switch (this.getJobStatus()) {
		case JOB_COMPLETED:
			return 0;
		case JOB_TODO:
			return this.videoTasks.size();
		default:
			int count = 0;
			for (VideoEncodingTask task : this.videoTasks) {
				if (task.getTaskState() == TaskState.TASK_TODO) {
					++count;
				}
			}
			return count;
		}
	}

	/**
	 * Compare job accordingly to priority, tasks remaining and job length.
	 * 
	 * @return 1 if this is bigger, -1 otherwise
	 */
	public int compareTo(Job other) {
		if (this.priority != other.priority) {
			return Integer.compare(this.priority, other.priority);
		} else if (this.getTaskRemainingCount() != other.getTaskRemainingCount()) {
			return Integer.compare(this.getTaskRemainingCount(), other.getTaskRemainingCount());
		} else {
			return Long.compare(this.getLengthOfJob(), other.getLengthOfJob());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Job)) {
			return false;
		}
		Job other = (Job) obj;
		return other.getJobId().equals(this.getJobId());
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}

	public JobState getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(JobState jobStatus) {
		this.jobStatus = jobStatus;
	}

	public long getFrameCount() {
		return frameCount;
	}

	public double getFrameRate() {
		return frameRate;
	}

	public int getLengthOfTasks() {
		return lengthOfTasks;
	}

	public void setLengthOfTasks(int lengthOfTasks) {
		this.lengthOfTasks = lengthOfTasks;
	}

	public long getLengthOfJob() {
		return lengthOfJob;
	}

	public void setLengthOfJob(long lengthOfJob) {
		this.lengthOfJob = lengthOfJob;
	}

	public ArrayList<Task> getTasks() {
		ArrayList<Task> tasks = new ArrayList<>();
		tasks.addAll(audioTasks);
		tasks.addAll(videoTasks);
		return tasks;
	}

	public void setTasks(ArrayList<VideoEncodingTask> tasks) {
		this.videoTasks = tasks;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public String getPartsFolderName() {
		return partsFolderName;
	}

	public void setPartsFolderName(String partsFolderName) {
		this.partsFolderName = partsFolderName;
	}

	public ArrayList<AudioEncodingTask> getAudioTasks() {
		return audioTasks;
	}

	public ArrayList<VideoEncodingTask> getVideoTasks() {
		return videoTasks;
	}

}
