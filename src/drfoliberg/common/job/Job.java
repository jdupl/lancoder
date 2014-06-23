package drfoliberg.common.job;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import drfoliberg.common.status.JobState;
import drfoliberg.common.status.TaskState;
import drfoliberg.common.task.video.VideoEncodingTask;

/**
 * A job is the whole process of taking the source file, splitting it if necessary, encoding it and merge back all
 * tracks. Tasks will be dispatched to nodes by the master.
 * 
 * @author justin
 * 
 */
public class Job extends JobConfig {

	private static final long serialVersionUID = -3817299446490049451L;
	private ArrayList<VideoEncodingTask> tasks;
	private String jobId;
	private String jobName;
	private JobState jobStatus;
	private int lengthOfTasks;
	private long lengthOfJob;
	private int frameCount;
	private float frameRate;
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

	public Job(JobConfig config, String jobName, int lengthOfTasks, long lengthOfJob, int frameCount, float frameRate,
			String encodingOutputFolder) {
		super(config);

		this.jobName = jobName;
		this.lengthOfTasks = lengthOfTasks;
		this.lengthOfJob = lengthOfJob;
		this.frameCount = frameCount;
		this.frameRate = frameRate;

		this.tasks = new ArrayList<>();
		this.jobStatus = JobState.JOB_TODO;
		this.partsFolderName = "parts"; // TODO Why would this change ? Perhaps move to constant.
		// Get source' filename 
		File source = new File(config.getSourceFile());
		// Set output's filename 
		this.outputFileName = String.format("%s.mkv", FilenameUtils.removeExtension(source.getName()));
		// Get /sharedFolder/LANcoder/jobsOutput/jobName/ (without the shared folder)
		File relativeEncodingOutput = FileUtils.getFile(encodingOutputFolder, jobName);
		this.outputFolder = relativeEncodingOutput.getPath();

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
		createTasks();
	}

	/**
	 * Creates tasks of the job with good handling of paths.
	 * 
	 */
	private void createTasks() {
		long currentMs = 0;
		int taskNo = 0;
		long remaining = lengthOfJob;

		// Get relative (to absolute shared directory) output folder for this job's tasks
		File relativeTasksOutput = FileUtils.getFile(this.getOutputFolder(), this.partsFolderName);
		while (remaining > 0) {
			VideoEncodingTask t = new VideoEncodingTask(taskNo, this); // hackish but should work for now TODO clean
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

			// Set task output file
			File relativeTaskOutputFile = FileUtils.getFile(relativeTasksOutput,
					String.format("part-%d.mpeg.ts", t.getTaskId())); // TODO check for extension
			t.setOutputFile(relativeTaskOutputFile.getPath());

			this.tasks.add(t);
			taskNo++;
		}
	}

	/**
	 * Returns next task to encode. Changes Job status if job is not started yet.
	 * 
	 * @return The task or null if no task is available
	 */
	public synchronized VideoEncodingTask getNextTask() {

		if (getCountTaskRemaining() == 0) {
			return null;
		}

		if (this.getJobStatus() == JobState.JOB_TODO) {
			// TODO move this to job manager
			this.setJobStatus(JobState.JOB_COMPUTING);
		}

		for (VideoEncodingTask task : this.tasks) {
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
			for (VideoEncodingTask task : this.tasks) {
				if (task.getStatus() == TaskState.TASK_TODO) {
					++count;
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

	public ArrayList<VideoEncodingTask> getTasks() {
		return tasks;
	}

	public void setTasks(ArrayList<VideoEncodingTask> tasks) {
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

	public RateControlType getRateContolType() {
		return rateControlType;
	}

	public void setRateContolType(RateControlType rateContolType) {
		this.rateControlType = rateContolType;
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

}
