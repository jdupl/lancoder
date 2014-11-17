package org.lancoder.common.status;

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
	 * All tasks in the job are encoded. This status should be temporary as it would go to muxing afterwards.
	 */
	JOB_ENCODED,
	/**
	 * Job is currently being muxed.
	 */
	JOB_MUXING,
	/**
	 * All tasks in the job are encoded and muxed.
	 */
	JOB_COMPLETED,
	/**
	 * Fatal failure occurred. Most likely a muxing error.
	 */
	JOB_FAILED

}
