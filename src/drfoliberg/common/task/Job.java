package drfoliberg.common.task;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import drfoliberg.common.FFMpegProber;

/**
 * A job is the whole process of taking the source file, spliting it if
 * necessary, encoding it and merge back all tracks. Tasks will be dispatched to
 * nodes by the master.
 * 
 * @author justin
 * 
 */
public class Job {
	private ArrayList<Task> tasks;
	private String jobId;
	private String jobName;
	private String sourceFile;
	private JobType jobType;
	private ArrayList<Task> processedTasks;
	private int lengthOfTasks;
	private long lengthOfJob;
	private long frameCount;
	private float frameRate;

	/**
	 * 
	 * @param jobName
	 *            The job name
	 * @param sourceFile
	 *            The source file to encode relative to the shared directory
	 * @param jobType
	 *            The type of bitrate control
	 * @param lengthOfTasks
	 *            The length of the tasks in ms that will be sent to worker (0 =
	 *            infinite)
	 */
	public Job(String jobName, String sourceFile, JobType jobType, int lengthOfTasks) {
		this.sourceFile = sourceFile;
		this.jobName = jobName;
		this.tasks = new ArrayList<>();
		this.jobType = jobType;
		this.processedTasks = new ArrayList<>();
		this.lengthOfTasks = lengthOfTasks;

		// get fps and ms duration from prober
		this.lengthOfJob = (long) (FFMpegProber.getSecondsDuration(sourceFile) * 1000);
		this.frameRate = FFMpegProber.getFrameRate(sourceFile);
		this.frameCount = (long) (lengthOfJob / 1000 * frameRate);

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
			EncodingTask t = new EncodingTask(taskNo++, sourceFile);
			t.setJobId(jobId);
			t.setStartTime(currentMs);
			if ((((double) remaining - this.lengthOfTasks) / this.lengthOfJob) <= 0.10) {
				System.out.println("next task will be too short, adding the ms to the current task");
				t.setEndTime(lengthOfJob);
				remaining = 0;
			} else {
				t.setEndTime(currentMs + lengthOfTasks);
				remaining -= lengthOfTasks;
				currentMs += lengthOfTasks;
			}
			long ms = t.getEndTime() - t.getStartTime();
			t.setEstimatedFrames((long) (ms / 1000 * frameRate));
			this.tasks.add(t);
		}
		System.out.println("Job was divided into " + this.tasks.size() + " tasks!");
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

	public JobType getJobType() {
		return jobType;
	}

	public void setJobType(JobType jobType) {
		this.jobType = jobType;
	}

	public ArrayList<Task> getProcessedTasks() {
		return processedTasks;
	}

	public void setProcessedTasks(ArrayList<Task> processedTasks) {
		this.processedTasks = processedTasks;
	}

}
