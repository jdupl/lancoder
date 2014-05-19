package drfoliberg.common.status;

/**
 * Possible states for a job.
 * 
 */
public enum JobState {
	/**
	 * Job has no task started.
	 */
	JOB_TODO,
	/**
	 * Some tasks are being encoded in the job.
	 */
	JOB_COMPUTING,
	/**
	 * Job is paused and tasks are not dispatched. State is not currently used (future use possibly).
	 */
	JOB_PAUSED,
	/**
	 * All tasks in the job are completed.
	 */
	JOB_COMPLETED
}
