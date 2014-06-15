package drfoliberg.common.job;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import drfoliberg.common.status.JobState;
import drfoliberg.common.status.TaskState;
import drfoliberg.common.task.Task;

/**
 * A job is the whole process of taking the source file, splitting it if necessary, encoding it and merge back all
 * tracks. Tasks will be dispatched to nodes by the master.
 * 
 * @author justin
 * 
 */
public class Job extends JobConfig {

	private static final long serialVersionUID = -3817299446490049451L;
	private ArrayList<Task> tasks;
	private String jobId;
	private String jobName;
	private JobState jobStatus;
	private int lengthOfTasks;
	private long lengthOfJob;
	private int frameCount;
	private float frameRate;

	/**
	 * @deprecated
	 * @param jobName
	 *            The job name
	 * @param sourceFile
	 *            The Source file to encode relative to the shared directory
	 * @param lengthOfTasks
	 *            The length of the tasks in ms that will be sent to worker (0 = infinite)
	 * @param lengthOfJob
	 *            Total Length of the job
	 * @param frameCount
	 *            The total frame count
	 * @param frameRate
	 *            The frame rate to encode at
	 */
	public Job(String jobName, String sourceFile, int lengthOfTasks, long lengthOfJob, int frameCount, float frameRate,
			int rate, FFmpegPreset preset, byte passes, RateControlType rateControlType) {

		super(sourceFile, rateControlType, rate, passes, preset, null);
		this.jobName = jobName;

		this.lengthOfTasks = lengthOfTasks;
		this.lengthOfJob = lengthOfJob;
		this.frameCount = frameCount;
		this.frameRate = frameRate;
		this.tasks = new ArrayList<>();
		this.jobStatus = JobState.JOB_TODO;

	}

	public Job(JobConfig config, String jobName, int lengthOfTasks, long lengthOfJob, int frameCount, float frameRate) {
		super(config);

		this.jobName = jobName;
		this.lengthOfTasks = lengthOfTasks;
		this.lengthOfJob = lengthOfJob;
		this.frameCount = frameCount;
		this.frameRate = frameRate;

		this.tasks = new ArrayList<>();
		this.jobStatus = JobState.JOB_TODO;

		createTasks();
	}

	private void createTasks() {
		long currentMs = 0;
		int taskNo = 0;
		long remaining = lengthOfJob;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] byteArray = md.digest((sourceFile + jobName + System.currentTimeMillis()).getBytes());
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

		while (remaining > 0) {
			Task t = new Task(taskNo++, this); // hackish but should work for now TODO clean
			t.setJobId(jobId);
			t.setEncodingStartTime(currentMs);
			if ((((double) remaining - this.lengthOfTasks) / this.lengthOfJob) <= 0.15) {
				t.setEncodingEndTime(lengthOfJob);
				remaining = 0;
			} else {
				t.setEncodingEndTime(currentMs + lengthOfTasks);
				remaining -= lengthOfTasks;
				currentMs += lengthOfTasks;
			}
			long ms = t.getEncodingEndTime() - t.getEncodingStartTime();
			t.setEstimatedFramesCount((long) Math.floor((ms / 1000 * frameRate)));

			this.tasks.add(t);
		}
	}

	/**
	 * Returns next task to encode. Changes Job status if job is not started yet.
	 * 
	 * @return The task or null if no task is available
	 */
	public synchronized Task getNextTask() {

		if (getCountTaskRemaining() == 0) {
			return null;
		}

		if (this.getJobStatus() == JobState.JOB_TODO) {
			// TODO perhaps move this
			this.setJobStatus(JobState.JOB_COMPUTING);
		}

		for (Task task : this.tasks) {
			if (task.getStatus() == TaskState.TASK_TODO) {
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
	public synchronized int getCountTaskRemaining() {

		switch (this.getJobStatus()) {
		case JOB_COMPLETED:
			return 0;
		case JOB_TODO:
			return this.tasks.size();
		default:
			int count = 0;
			for (Task task : this.tasks) {
				if (task.getStatus() == TaskState.TASK_TODO) {
					--count;
				}
			}
			return count;
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

	public JobState getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(JobState jobStatus) {
		this.jobStatus = jobStatus;
	}

	public long getFrameCount() {
		return frameCount;
	}

	public float getFrameRate() {
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
		return tasks;
	}

	public void setTasks(ArrayList<Task> tasks) {
		this.tasks = tasks;
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

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public RateControlType getRateContolType() {
		return rateControlType;
	}

	public void setRateContolType(RateControlType rateContolType) {
		this.rateControlType = rateContolType;
	}

}
