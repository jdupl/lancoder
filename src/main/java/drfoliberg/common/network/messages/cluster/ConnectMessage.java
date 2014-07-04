package main.java.drfoliberg.common.network.messages.cluster;

import java.io.Serializable;
import java.net.InetAddress;

import main.java.drfoliberg.common.network.ClusterProtocol;
import main.java.drfoliberg.common.status.NodeState;

public class ConnectMessage extends AuthMessage implements Serializable {

	private static final long serialVersionUID = 831513295350691753L;

	public int localPort;
	public String name;
	@Deprecated
	public NodeState status;
	public InetAddress address;

	/**
	 * Message object sent from workers to master to connect. The same object is replied to the worker so it can grab
	 * it's UNID or confirm the reconnection.
	 * 
	 * @param unid
	 *            Current worker UNID (or empty string) Master might send a new UNID if not in memory.
	 * @param localPort
	 *            The port the worker server is listening on.
	 * 
	 * @param name
	 *            the name given to the worker
	 * @param status
	 *            the status (connect or disconnect)
	 */
	@Deprecated
	public ConnectMessage(String unid, int localPort, String name, NodeState status) {
		super(ClusterProtocol.CONNECT_ME, unid);
		this.unid = unid;
		this.localPort = localPort;
		this.name = name;
		this.status = status;
	}
	/**
	 * Message object sent from workers to master to connect. The same object is replied to the worker so it can grab
	 * it's UNID or confirm the reconnection.
	 * 
	 * @param unid
	 *            Current worker UNID (or empty string) Master might send a new UNID if not in memory.
	 * @param localPort
	 *            The port the worker server is listening on.
	 * 
	 * @param name
	 *            the name given to the worker
	 * @param address
	 *            the node address
	 */
	public ConnectMessage(String unid, int localPort, String name, InetAddress address) {
		super(ClusterProtocol.CONNECT_ME, unid);
		this.unid = unid;
		this.localPort = localPort;
		this.name = name;
		this.address = address;
	}

}
