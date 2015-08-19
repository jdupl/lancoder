package org.lancoder.common.job;

import java.io.File;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import org.lancoder.common.annotations.NoWebUI;
import org.lancoder.common.file_components.FileInfo;
import org.lancoder.common.file_components.streams.Stream;
import org.lancoder.common.file_components.streams.original.OriginalVideoStream;
import org.lancoder.common.status.JobState;
import org.lancoder.common.status.TaskState;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.Task;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.common.utils.FileUtils;

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
	private String relativeOutputFolder;
	/**
	 * Filename for final output file relative to outputFolder
	 */
	private String relativeFinalOutputFile;
	/**
	 * The folder in which to store the parts before muxing
	 */
	private String relativePartsFolder;
	private String relaiveSourceFile;

	private long timeAdded;
	private long timeStarted;
	private long timeCompleted;

	private ArrayList<Task> tasks = new ArrayList<>();
	/**
	 * List of ClientTasks. Only used by dispatcher and should not be serialized to webui.
	 */
	@NoWebUI
	private ArrayList<ClientTask> clientTasks = new ArrayList<>();
	/**
	 * Mapping of the destination streams and their tasks. Only used internally and should not be serialized to webui.
	 */
	@NoWebUI
	private HashMap<Stream, ArrayList<ClientTask>> streamTaskMapping = new HashMap<>();

	@NoWebUI
	private FileInfo fileinfo;

	public Job(String jobName, String sourceFile, int lengthOfTasks, FileInfo fileInfo, File outputFolder,
			String outputFileName) {
		this.jobId = generateId(sourceFile, jobName);
		this.jobName = jobName;
		this.lengthOfTasks = lengthOfTasks;
		this.lengthOfJob = fileInfo.getDuration();
		this.timeAdded = System.currentTimeMillis();
		this.fileinfo = fileInfo;

		OriginalVideoStream mainVideoStream = fileInfo.getMainVideoStream();
		if (mainVideoStream != null) {
			this.frameRate = mainVideoStream.getFrameRate();
			// Estimate the frame count from the frame rate and length
			this.frameCount = (int) Math.floor((lengthOfJob / 1000 * frameRate));
		}

		this.relaiveSourceFile = sourceFile;
		this.relativeFinalOutputFile = outputFileName;


		// Set output's filename
		this.relativeOutputFolder = FileUtils.getFile(outputFolder.getPath(), jobName).getPath();
		this.relativePartsFolder = FileUtils.getFile(this.relativeOutputFolder, "parts", jobId).getPath();
	}

	/**
	 * Create a unique id for a job. Uses the source file, the name of the job and the current millisecond time to avoid
	 * time, name and file collisions.
	 *
	 * @param sourceFile
	 *            The source file of the job
	 * @param jobName
	 *            The name of the job
	 * @return A unique id
	 */
	private static String generateId(String sourceFile, String jobName) {
		String result = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] byteArray = md.digest((sourceFile + jobName + System.currentTimeMillis()).getBytes());

			for (int i = 0; i < byteArray.length; i++) {
				result += Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			// even if the algorithm is not available, don't crash
			result = String.valueOf(System.currentTimeMillis());
		}
		return result;
	}

	public void complete() {
		this.jobStatus = JobState.JOB_COMPLETED;
		this.timeCompleted = System.currentTimeMillis();
	}

	public void cancel() {
		this.jobStatus = JobState.JOB_CANCELED;
	}

	public void muxing() {
		this.jobStatus = JobState.JOB_MUXING;
	}

	public void start(boolean initJob) {
		this.jobStatus = JobState.JOB_COMPUTING;
		if (initJob) {
			this.timeStarted = System.currentTimeMillis();
		}
	}

	public void start() {
		start(true);
	}

	public void fail() {
		this.jobStatus = JobState.JOB_FAILED;
	}

	public boolean isStarted() {
		return getJobStatus() != JobState.JOB_TODO;
	}

	/**
	 * Add a stream and its tasks to the job.
	 *
	 * @param stream
	 *            The destination stream
	 * @param clientTasks
	 *            The tasks
	 */
	public void addStream(Stream stream, ArrayList<ClientTask> clientTasks) {
		this.streamTaskMapping.put(stream, clientTasks);
		registerTasks(clientTasks);
	}

	private void registerTasks(ArrayList<ClientTask> tasks) {
		for (ClientTask clientTask : tasks) {
			registerTask(clientTask);
		}
	}

	private void registerTask(ClientTask clientTask) {
		this.clientTasks.add(clientTask);
		this.tasks.add(clientTask.getTask());
	}

	/**
	 * Get the tasks associated to this stream.
	 *
	 * @param stream
	 *            The stream to look for
	 * @return An ArrayList of related tasks
	 */
	public ArrayList<ClientTask> getTasksForStream(Stream stream) {
		return streamTaskMapping.get(stream);
	}

	/**
	 * Returns next task to encode. Changes Job status if job is not started yet.
	 *
	 * @return The task or null if no task is available
	 */
	public synchronized ClientVideoTask getNextVideoTask() {
		ArrayList<ClientVideoTask> tasks = getTodoVideoTask();
		return tasks.isEmpty() ? null : tasks.get(0);
	}

	/**
	 * Returns all tasks to do for this job. Changes Job status if job is not started yet.
	 *
	 * @return The list of task
	 */
	public synchronized ArrayList<ClientVideoTask> getTodoVideoTask() {
		ArrayList<ClientVideoTask> tasks = new ArrayList<>();
		for (ClientVideoTask task : this.getClientVideoTasks()) {
			if (task.getProgress().getTaskState() == TaskState.TASK_TODO) {
				tasks.add(task);
			}
		}
		return tasks;
	}

	public ArrayList<ClientAudioTask> getTodoAudioTask() {
		ArrayList<ClientAudioTask> tasks = new ArrayList<>();
		for (ClientAudioTask task : this.getClientAudioTasks()) {
			if (task.getProgress().getTaskState() == TaskState.TASK_TODO) {
				tasks.add(task);
			}
		}
		return tasks;
	}

	/**
	 * Counts if necessary the tasks currently not processed. A task being processed by a node counts as processed.
	 *
	 * @return The count of tasks left to dispatch
	 */
	public synchronized int getTodoTaskCount() {
		switch (this.getJobStatus()) {
		case JOB_COMPLETED:
			return 0;
		case JOB_TODO:
			return this.tasks.size();
		default:
			return getTodoTasks().size();
		}
	}

	public synchronized ArrayList<ClientTask> getTodoTasks() {
		ArrayList<ClientTask> todoTasks = new ArrayList<>();
		todoTasks.addAll(getTodoAudioTask());
		todoTasks.addAll(getTodoVideoTask());
		return todoTasks;
	}

	/**
	 * Counts if necessary the completed tasks.
	 *
	 * @return The count of tasks completed
	 */
	public synchronized int getTaskDoneCount() {
		switch (this.getJobStatus()) {
		case JOB_COMPLETED:
			return this.tasks.size();
		case JOB_TODO:
			return 0;
		default:
			int count = 0;
			for (Task task : this.tasks) {
				if (task.getProgress().getTaskState() == TaskState.TASK_COMPLETED) {
					++count;
				}
			}
			return count;
		}
	}

	public ArrayList<ClientAudioTask> getClientAudioTasks() {
		ArrayList<ClientAudioTask> tasks = new ArrayList<>();
		for (ClientTask task : this.clientTasks) {
			if (task instanceof ClientAudioTask) {
				tasks.add((ClientAudioTask) task);
			}
		}
		return tasks;
	}

	public ArrayList<ClientVideoTask> getClientVideoTasks() {
		ArrayList<ClientVideoTask> tasks = new ArrayList<>();
		for (ClientTask task : this.clientTasks) {
			if (task instanceof ClientVideoTask) {
				tasks.add((ClientVideoTask) task);
			}
		}
		return tasks;
	}

	/**
	 * Compare job accordingly to priority, tasks remaining and job length.
	 *
	 * @return 1 if this is bigger, -1 otherwise
	 */
	@Override
	public int compareTo(Job other) {
		if (this.priority != other.priority) {
			return Integer.compare(this.priority, other.priority);
		} else if (this.getTodoTaskCount() != other.getTodoTaskCount()) {
			return Integer.compare(this.getTodoTaskCount(), other.getTodoTaskCount());
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

	public ArrayList<Stream> getStreams() {
		ArrayList<Stream> streams = new ArrayList<>();
		streams.addAll(this.streamTaskMapping.keySet());
		return streams;
	}

	public ArrayList<ClientTask> getClientTasks() {
		return clientTasks;
	}

	public String getSourceFile() {
		return relaiveSourceFile;
	}

	public int getTaskCount() {
		return this.tasks.size();
	}

	public JobState getJobStatus() {
		return jobStatus;
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
		return relativeOutputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.relativeOutputFolder = outputFolder;
	}

	public String getOutputFileName() {
		return relativeFinalOutputFile;
	}

	public void setOutputFileName(String outputFileName) {
		this.relativeFinalOutputFile = outputFileName;
	}

	public String getPartsFolderName() {
		return relativePartsFolder;
	}

	public void setPartsFolderName(String partsFolderName) {
		this.relativePartsFolder = partsFolderName;
	}

	public long getTimeAdded() {
		return timeAdded;
	}

	public long getTimeStarted() {
		return timeStarted;
	}

	public long getTimeCompleted() {
		return timeCompleted;
	}

	public boolean isCompleted() {
		return getJobStatus() == JobState.JOB_COMPLETED;
	}

	public FileInfo getFileinfo() {
		return fileinfo;
	}
}
