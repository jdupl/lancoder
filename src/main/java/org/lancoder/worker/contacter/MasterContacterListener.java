package org.lancoder.worker.contacter;

import org.lancoder.common.network.cluster.messages.ConnectMessage;
import org.lancoder.common.status.NodeState;

public interface MasterContacterListener {

	/**
	 * Generate a node representation extending Message to send to the master.
	 * 
	 * @return
	 */
	public ConnectMessage getConnectMessage();

	/**
	 * Master sent unique node id. This also means the Worker is now connected.
	 * 
	 * @param unid
	 */
	public void receivedUnid(String unid);

	/**
	 * Behavior when master is not reachable
	 */
	public void masterTimeout();

	/**
	 * Get node's state.
	 */
	public NodeState getStatus();

}
