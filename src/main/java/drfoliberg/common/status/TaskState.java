package main.java.drfoliberg.common.status;

/**
 * Possible states for a task.
 * 
 */

public enum TaskState {
	/**
	 * Task is completed and saved on the shared disk.
	 */
	TASK_COMPLETED,
	/**
	 * Task is not assigned nor done.
	 */
	TASK_TODO,
	/**
	 * Task is assigned and currently encoding.
	 */
	TASK_COMPUTING,
	/**
	 * Task was cancelled by a node (possibly a crash or shutdown). This state is temporary as it should go back to
	 * TASK_TODO to be picked up by another node.
	 */
	TASK_CANCELED,
	/**
	 * Task is assigned, but not started (should only be temporary)
	 */
	TASK_ASSIGNED
}
