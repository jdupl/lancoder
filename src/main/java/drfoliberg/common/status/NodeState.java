package drfoliberg.common.status;

/**
 * Possible states for a node.
 * 
 */
public enum NodeState {
	/**
	 * Node is not connected.
	 */
	NOT_CONNECTED,
	/**
	 * Node is connected and may received tasks from master.
	 */
	FREE,
	/**
	 * Node is currently working on a task.
	 */
	WORKING,
	/**
	 * Node is paused. State is not currently used (future use possibly).
	 */
	PAUSED,
	/**
	 * Node is crashed but is connected. No work should be sent as manual investigation is necessary.
	 */
	CRASHED,
	/**
	 * Master is currently waiting for the node
	 */
	LOCKED
}
