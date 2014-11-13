package org.lancoder.master;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.lancoder.common.Node;
import org.lancoder.common.codecs.Codec;
import org.lancoder.common.events.Event;
import org.lancoder.common.events.EventEnum;
import org.lancoder.common.events.EventListener;
import org.lancoder.common.job.Job;
import org.lancoder.common.network.cluster.messages.TaskRequestMessage;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.status.TaskState;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.master.dispatcher.DispatchItem;
import org.lancoder.master.dispatcher.DispatcherPool;

public class JobManager {

	private EventListener listener;
	private NodeManager nodeManager;
	private DispatcherPool dispatcherPool;
	/**
	 * HashMap of the jobs. Key is the job's id for fast access.
	 */
	private final HashMap<String, Job> jobs = new HashMap<>();
	/**
	 * Mapping of the current tasks of the cluster. The key is the node processing the task.
	 */
	private final HashMap<ClientTask, Node> assignments = new HashMap<>();

	public JobManager(EventListener listener, NodeManager nodeManager, DispatcherPool dispatcherPool,
			MasterSavedInstance savedInstance) {
		this.listener = listener;
		this.nodeManager = nodeManager;
		this.dispatcherPool = dispatcherPool;
		if (savedInstance != null) {
			this.jobs.putAll(savedInstance.getJobs());
		}
	}

	public HashMap<String, Job> getJobHashMap() {
		return jobs;
	}

	public boolean addJob(Job j) {
		if (this.jobs.put(j.getJobId(), j) != null) {
			return false;
		}
		System.out.printf("Job %s added.%n", j.getJobName());
		updateNodesWork();
		return true;
	}

	public boolean deleteJob(Job j) {
		if (j == null) {
			return false;
		}
		for (Node node : nodeManager.getNodes()) {
			ArrayList<ClientTask> nodeTasks = new ArrayList<>(node.getCurrentTasks());
			for (ClientTask task : nodeTasks) {
				if (task.getJobId().equals(j.getJobId())) {
					unassignTask(task, node);
				}
			}
		}
		if (this.jobs.remove(j.getJobId()) == null) {
			return false;
		}
		this.listener.handle(new Event(EventEnum.CONFIG_UPDATED));
		updateNodesWork();
		return true;
	}

	private void unassignTask(ClientTask task, Node assigne) {
		dispatcherPool.handle(new DispatchItem(new TaskRequestMessage(task, ClusterProtocol.UNASSIGN_TASK), assigne));
		task.getProgress().reset();
		taskUpdated(task, assigne);
	}

	public ArrayList<Job> getJobs() {
		ArrayList<Job> jobs = new ArrayList<>();
		for (Entry<String, Job> e : this.jobs.entrySet()) {
			jobs.add(e.getValue());
		}
		return jobs;
	}

	private ClientAudioTask getNextAudioTask(ArrayList<Codec> codecs) {
		ClientAudioTask task = null;
		ArrayList<Job> jobList = new ArrayList<>(jobs.values());
		Collections.sort(jobList);
		for (Iterator<Job> itJob = jobList.iterator(); itJob.hasNext() && task == null;) {
			Job job = itJob.next();
			ArrayList<ClientAudioTask> tasks = job.getTodoAudioTask();
			for (Iterator<ClientAudioTask> itTask = tasks.iterator(); itTask.hasNext() && task == null;) {
				ClientAudioTask clientTask = itTask.next();
				if (codecs.contains(clientTask.getStreamConfig().getOutStream().getCodec())) {
					task = clientTask;
				}
			}
		}
		return task;
	}

	private ClientVideoTask getNextVideoTask(ArrayList<Codec> codecs) {
		ClientVideoTask task = null;
		ArrayList<Job> jobList = new ArrayList<>(jobs.values());
		Collections.sort(jobList);
		for (Iterator<Job> itJob = jobList.iterator(); itJob.hasNext() && task == null;) {
			Job job = itJob.next();
			ArrayList<ClientVideoTask> tasks = job.getTodoVideoTask();
			for (Iterator<ClientVideoTask> itTask = tasks.iterator(); itTask.hasNext() && task == null;) {
				ClientVideoTask clientTask = itTask.next();
				if (codecs.contains(clientTask.getStreamConfig().getOutStream().getCodec())) {
					task = clientTask;
				}
			}
		}
		return task;
	}

	/**
	 * Checks if any task and nodes are available and dispatch until possible. Will only dispatch tasks to nodes that
	 * are capable of encoding with the desired library. Always put audio tasks in priority.
	 */
	public synchronized void updateNodesWork() {
		for (Node node : nodeManager.getFreeAudioNodes()) {
			ClientAudioTask task = getNextAudioTask(node.getCodecs());
			if (task != null) {
				dispatch(task, node);
				break;
			}
		}
		for (Node node : nodeManager.getFreeVideoNodes()) {
			ClientVideoTask task = getNextVideoTask(node.getCodecs());
			if (task != null) {
				dispatch(task, node);
				break;
			}
		}
		this.listener.handle(new Event(EventEnum.CONFIG_UPDATED));
	}

	public void dispatch(ClientTask task, Node node) {
		if (assign(task, node)) {
			task.getProgress().start();
			node.lock();
			dispatcherPool.handle(new DispatchItem(new TaskRequestMessage(task), node));
		}
	}

	/**
	 * Internally assign node to task in cluster task-node mapping.
	 * 
	 * @param task
	 *            The task (used as key)
	 * @param node
	 *            The node working on the task (used as value)
	 * @return True if task was assigned. False if task is already mapped.
	 */
	private synchronized boolean assign(ClientTask task, Node node) {
		boolean assigned = false;
		if (!assignments.containsKey(task)) {
			this.assignments.put(task, node);
			node.addTask(task);
			assigned = true;
		}
		return assigned;
	}

	private synchronized boolean unassign(ClientTask task) {
		boolean unassigned = false;
		Node previousAssignee = this.assignments.remove(task);
		if (previousAssignee != null) {
			unassigned = true;
			previousAssignee.getCurrentTasks().remove(task);
		}
		return unassigned;
	}

	public boolean taskUpdated(ClientTask task, Node n) {
		TaskState updateStatus = task.getProgress().getTaskState();
		switch (updateStatus) {
		case TASK_COMPLETED:
			unassign(task);
			Job job = this.jobs.get(task.getJobId());
			if (job.getTaskDoneCount() == job.getTaskCount()) {
				listener.handle(new Event(EventEnum.JOB_ENCODING_COMPLETED, job));
			}
			updateNodesWork();
			break;
		case TASK_TODO:
		case TASK_CANCELED:
			unassign(task);
			task.getProgress().reset();
			updateNodesWork();
			break;
		default:
			break;
		}
		return false;
	}

	public Job getJob(String jobId) {
		return jobs.get(jobId);
	}
}
