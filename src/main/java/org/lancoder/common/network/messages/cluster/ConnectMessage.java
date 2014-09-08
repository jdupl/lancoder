package org.lancoder.common.network.messages.cluster;

import java.net.InetAddress;
import java.util.ArrayList;

import org.lancoder.common.codecs.Codec;
import org.lancoder.common.network.messages.ClusterProtocol;

public class ConnectMessage extends AuthMessage {

	private static final long serialVersionUID = 831513295350691753L;

	public int localPort;
	public String name;
	public InetAddress address;
	public ArrayList<Codec> codecs;
	public int threadCount;

	/**
	 * Message object sent from workers to master to connect. The same object is replied to the worker so it can grab
	 * it's UNID or confirm the reconnection.
	 * 
	 * @param unid
	 *            Current worker UNID (or empty string) Master might send a new UNID if not in memory.
	 * @param localPort
	 *            The port the worker server is listening on.
	 * @param name
	 *            the name given to the worker
	 * @param address
	 *            the node address
	 * @param codecs
	 *            List of supported encoders by the node
	 * @param threadCount
	 *            Maximum threads that can be used
	 */
	public ConnectMessage(String unid, int localPort, String name, InetAddress address, ArrayList<Codec> codecs,
			int threadCount) {
		super(ClusterProtocol.CONNECT_ME, unid);
		this.unid = unid;
		this.localPort = localPort;
		this.name = name;
		this.address = address;
		this.codecs = codecs;
		this.threadCount = threadCount;
	}

}
