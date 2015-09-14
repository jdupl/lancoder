package org.lancoder.common;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.codecs.CodecLoader;
import org.lancoder.common.codecs.base.Codec;
import org.lancoder.common.status.NodeState;
import org.lancoder.common.task.ClientTask;

public class Node implements Serializable {

	private static final long serialVersionUID = 3450445684775221368L;
	private InetAddress nodeAddress;
	private int nodePort;
	private NodeState status = NodeState.NOT_CONNECTED;
	private String name;
	private String unid;
	private int threadCount;
	private ArrayList<ClientTask> currentTasks = new ArrayList<>();
	private ArrayList<ClientTask> pendingTasks = new ArrayList<>();
	private ArrayList<Codec> codecs = new ArrayList<>();
	private boolean locked = false;
	private int failureCount;

	public Node(InetAddress nodeAddress, int nodePort, String name, ArrayList<CodecEnum> codecs, int threadCount,
			String unid) {
		this.nodeAddress = nodeAddress;
		this.nodePort = nodePort;
		this.name = name;
		this.unid = unid;
		this.threadCount = threadCount;

		for (CodecEnum codecEnum : codecs) {
			this.codecs.add(CodecLoader.fromCodec(codecEnum));
		}
	}

	public void failure() {
		this.failureCount++;
	}

	public boolean isLocked() {
		return locked;
	}

	public void lock() {
		this.locked = true;
	}

	public void unlock() {
		this.locked = false;
	}

	public boolean hasTask(ClientTask task) {
		for (ClientTask nodeTask : this.getAllTasks()) {
			if (nodeTask.equals(task)) {
				return true;
			}
		}
		for (ClientTask nodeTask : this.getPendingTasks()) {
			if (nodeTask.equals(task)) {
				return true;
			}
		}
		return false;
	}

	public void addPendingTask(ClientTask task) {
		pendingTasks.add(task);
	}

	public ArrayList<ClientTask> getAllTasks() {
		ArrayList<ClientTask> tasks = new ArrayList<>();
		tasks.addAll(currentTasks);
		tasks.addAll(pendingTasks);
		return tasks;
	}

	public void addTask(ClientTask currentTask) {
		this.currentTasks.add(currentTask);
	}

	/**
	 * Method to check if the current supports the codec of the output stream of a task.
	 *
	 * @param task
	 *            The task to check
	 * @return True if node can handle the task
	 */
	public boolean canHandle(ClientTask task) {
		Codec taskCodec = task.getStreamConfig().getOutStream().getCodec();
		return this.codecs.contains(taskCodec);
	}

	@Override
	public boolean equals(Object o) {
		boolean equals = false;
		if (o instanceof Node) {
			Node n = (Node) o;
			if (n.getNodeAddress().equals(this.getNodeAddress()) && n.getNodePort() == this.getNodePort()) {
				equals = true;
			}
		}
		return equals;
	}

	public void removeAllTasks() {
		currentTasks.clear();
		pendingTasks.clear();
	}

	public void confirm(ClientTask task) {
		pendingTasks.remove(task);
		currentTasks.add(task);
	}

	public void removeTask(ClientTask task) {
		currentTasks.remove(task);
		pendingTasks.remove(task);
	}

	@Override
	public String toString() {
		return "Node [nodeAddress=" + nodeAddress + ", nodePort=" + nodePort + ", status=" + status + ", name=" + name
				+ ", unid=" + unid + ", currentTasks=" + currentTasks + ", codecs=" + codecs + "]";
	}

	public int getFailureCount() {
		return failureCount;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public ArrayList<Codec> getCodecs() {
		return codecs;
	}

	public ArrayList<ClientTask> getPendingTasks() {
		return pendingTasks;
	}

	public InetAddress getNodeAddress() {
		return nodeAddress;
	}

	public void setNodeAddress(InetAddress nodeAddress) {
		this.nodeAddress = nodeAddress;
	}

	public NodeState getStatus() {
		return status;
	}

	public void setStatus(NodeState status) {
		this.status = status;
	}

	public int getNodePort() {
		return nodePort;
	}

	public void setNodePort(int nodePort) {
		this.nodePort = nodePort;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<ClientTask> getCurrentTasks() {
		return currentTasks;
	}

	public String getUnid() {
		return unid;
	}

	public void setUnid(String nodeIdentifier) {
		this.unid = nodeIdentifier;
	}

}
