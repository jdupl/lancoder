package org.lancoder.common.network.cluster.protocol;

public enum ClusterProtocol {
	/**
	 * Message sent and handled successfully
	 */
	OK,
	/**
	 * Close the socket.
	 */
	BYE,
	/**
	 * A full status update is requested by the sender. (can be master or node)
	 **/
	STATUS_REQUEST,
	/**
	 * A task request from master to a node.
	 */
	TASK_REQUEST,
	/**
	 * This response is a full status report.
	 */
	STATUS_REPORT,
	/**
	 * Crash Report (see issue #4)
	 */
	CRASH_REPORT,
	/**
	 * Connection request of node.
	 */
	CONNECT_REQUEST,
	/**
	 * Connection response of master.
	 */
	CONNECT_RESPONSE,
	/**
	 * Disconnection request TODO refactor to 'disconnection_request'
	 */
	DISCONNECT_ME,
	/**
	 * When node was successfully connected, master sends the id of the node.
	 */
	NEW_UNID,
	/**
	 * Worker could not start task. In case of double assignment.
	 */
	TASK_REFUSED,
	/**
	 * Worker was free and could start the task.
	 */
	TASK_ACCEPTED,
	/**
	 * Node refused to speak to master (probably key verification)
	 **/
	BAD_REQUEST,
	/**
	 * Master refused to speak to node (key verification)
	 */
	BAD_NODE,
	/**
	 * Represents a simple ping.
	 */
	PING,
	/**
	 * Response of the ping.
	 */
	PONG
}