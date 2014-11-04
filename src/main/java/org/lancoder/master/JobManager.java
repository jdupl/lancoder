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
	private final HashMap<String, Job> jobs = new HashMap<>();

	public JobManager(EventListener listener, NodeManager nodeManager, DispatcherPool dispatcherPool) {
		this.listener = listener;
		this.nodeManager = nodeManager;
		this.dispatcherPool = dispatcherPool;
	}

	public boolean addJob(Job j) {
		System.out.println("job " + j.getJobName() + " added");
		if (this.jobs.put(j.getJobId(), j) != null) {
			return false;
		}
		this.listener.handle(new Event(EventEnum.CONFIG_UPDATED));
		updateNodesWork();
		return true;
	}

	public boolean deleteJob(Job j) {
		if (j == null) {
			return false;
		}
		for (Node node : nodeManager.getNodes()) {
			for (ClientTask task : node.getCurrentTasks()) {
				if (task.getJobId().equals(j.getJobId())) {
					task.getProgress().reset();
					taskUpdated(task, node);
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
		System.out.println("Trying to dispatch to " + node.getName() + " task " + task.getTaskId() + " from "
				+ task.getJobId());
		if (task.getProgress().getTaskState() == TaskState.TASK_TODO) {
			task.getProgress().start();
		}
		node.lock();
		task.getProgress().start();
		node.addTask(task);
		dispatcherPool.handle(new DispatchItem(new TaskRequestMessage(task), node));
	}

	public boolean taskUpdated(ClientTask task, Node n) {
		TaskState updateStatus = task.getProgress().getTaskState();
		switch (updateStatus) {
		case TASK_COMPLETED:
			n.getCurrentTasks().remove(task);
			Job job = this.jobs.get(task.getJobId());
			boolean jobDone = true;
			for (ClientTask t : job.getClientTasks()) {
				if (t.getProgress().getTaskState() != TaskState.TASK_COMPLETED) {
					jobDone = false;
					break;
				}
			}
			if (jobDone) {
				listener.handle(new Event(EventEnum.JOB_ENCODING_COMPLETED, job));
			}
			updateNodesWork();
			break;
		case TASK_TODO:
		case TASK_CANCELED:
			task.getProgress().reset();
			n.getCurrentTasks().remove(task);
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
