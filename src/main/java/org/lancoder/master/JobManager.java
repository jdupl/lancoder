package org.lancoder.master;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.lancoder.common.Node;
import org.lancoder.common.codecs.base.Codec;
import org.lancoder.common.events.Event;
import org.lancoder.common.events.EventEnum;
import org.lancoder.common.events.EventListener;
import org.lancoder.common.job.Job;
import org.lancoder.common.network.cluster.messages.TaskRequestMessage;
import org.lancoder.common.network.cluster.protocol.ClusterProtocol;
import org.lancoder.common.network.messages.web.ApiJobRequest;
import org.lancoder.common.status.JobState;
import org.lancoder.common.status.TaskState;
import org.lancoder.common.strategies.stream.EncodeStrategy;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.master.dispatcher.DispatchItem;
import org.lancoder.master.dispatcher.DispatcherPool;

public class JobManager implements EventListener {

	private static final long DISPATCH_DELAY_MSEC = 5000;
	private EventListener listener;
	private NodeManager nodeManager;
	private DispatcherPool dispatcherPool;
	private JobInitiator jobInitiator;
	/**
	 * HashMap of the jobs. Key is the job's id for fast access.
	 */
	private final HashMap<String, Job> jobs = new HashMap<>();
	/**
	 * Mapping of the current tasks of the cluster.
	 */
	private ConcurrentHashMap<ClientTask, Assignment> assignments = new ConcurrentHashMap<>();

	private Logger logger = Logger.getLogger("lancoder");

	public JobManager(EventListener listener, NodeManager nodeManager, DispatcherPool dispatcherPool,
			MasterSavedInstance savedInstance, JobInitiator jobInitiator) {
		this.listener = listener;
		this.nodeManager = nodeManager;
		this.dispatcherPool = dispatcherPool;
		this.jobInitiator = jobInitiator;

		if (savedInstance != null) {
			this.jobs.putAll(savedInstance.getJobs());
		}

		this.removeLastInstanceInProgressTasks();
	}

	public HashMap<String, Job> getJobHashMap() {
		return jobs;
	}

	public boolean addJob(Job j) {
		if (this.jobs.put(j.getJobId(), j) != null) {
			return false;
		}

		logger.fine(String.format("Job %s added.%n", j.getJobName()));
		this.listener.handle(new Event(EventEnum.WORK_NEEDS_UPDATE));
		return true;
	}

	public boolean deleteJob(Job j) {
		if (j == null) {
			return false;
		}

		j.cancel();

		for (Node node : nodeManager.getNodes()) {
			ArrayList<ClientTask> nodeTasks = new ArrayList<>(node.getCurrentTasks());
			nodeTasks.addAll(node.getPendingTasks());
			for (ClientTask task : nodeTasks) {
				if (task.getJobId().equals(j.getJobId())) {
					unassignTask(task, node);
				}
			}
		}

		if (this.jobs.remove(j.getJobId()) == null) {
			return false;
		}

//		this.listener.handle(new Event(EventEnum.CONFIG_UPDATED));
		this.listener.handle(new Event(EventEnum.WORK_NEEDS_UPDATE));

		return true;
	}

	/**
	 * Notify a node that a task was unassigned.
	 *
	 * @param task
	 *            The task to unassign
	 * @param assigne
	 *            The node currently processing the task
	 */
	private void unassignTask(ClientTask task, Node assigne) {
		dispatcherPool.add(new DispatchItem(new TaskRequestMessage(task, ClusterProtocol.UNASSIGN_TASK), assigne));
		task.cancel();
		taskUpdated(task, assigne);
	}

	public ArrayList<Job> getJobs() {
		ArrayList<Job> jobs = new ArrayList<>();

		for (Entry<String, Job> e : this.jobs.entrySet()) {
			jobs.add(e.getValue());
		}
		Collections.sort(jobs);
		return jobs;
	}

	public ArrayList<Job> getAvailableJobs() {
		ArrayList<Job> jobs = new ArrayList<>();
		for (Entry<String, Job> e : this.jobs.entrySet()) {
			Job job = e.getValue();
			if (job.getJobStatus() == JobState.JOB_COMPUTING || job.getJobStatus() == JobState.JOB_TODO) {
				jobs.add(e.getValue());
			}
		}
		return jobs;
	}

	private ClientVideoTask getNextVideoTask(ArrayList<Codec> codecs) {
		ClientVideoTask task = null;
		ArrayList<Job> jobList = getAvailableJobs();
		Collections.sort(jobList);

		for (Iterator<Job> itJob = jobList.iterator(); itJob.hasNext() && task == null;) {
			Job job = itJob.next();
			ArrayList<ClientVideoTask> tasks = job.getTodoVideoTasks();

			for (Iterator<ClientVideoTask> itTask = tasks.iterator(); itTask.hasNext() && task == null;) {
				ClientVideoTask clientTask = itTask.next();
				EncodeStrategy encodeStrategy = (EncodeStrategy) clientTask.getStreamConfig().getOutStream()
						.getStrategy();

				if (codecs.contains(encodeStrategy.getCodec())) {
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
		Queue<ClientAudioTask> priorityAudioTasks= new ArrayDeque<>();
		ArrayList<Node> audioNodes = new ArrayList<>();
		ArrayList<Node> freeAudioNodes = new ArrayList<>();
		int totalAudioWorkersCount = 0;

		for (Job job : getJobs()) {
			if (job.getJobStatus() == JobState.JOB_COMPUTING || job.getJobStatus() == JobState.JOB_TODO) {
				ArrayList<ClientAudioTask> tasks = job.getTodoAudioTasks();
				for (ClientAudioTask task: tasks) {
					if (!task.getStreamConfig().getOutStream().getStrategy().isCopy())
						priorityAudioTasks.add(task);
				}
			}
		}

		for (Node node : nodeManager.getOnlineNodes()) {
			int nodeAudioWorkersCount = 0;

			for (ClientTask task : node.getAllTasks()) {
				if (task instanceof ClientAudioTask) {
					nodeAudioWorkersCount++;
				}
			}

			if (nodeAudioWorkersCount > 0) {
				totalAudioWorkersCount += nodeAudioWorkersCount;
				audioNodes.add(node);
				if (nodeManager.isAvailable(node))
					freeAudioNodes.add(node);
			}
		}

		ArrayList<Node> freeNodes = nodeManager.getFreeNodes();

		// Check if it should add an audio worker
		if (!priorityAudioTasks.isEmpty() && !freeNodes.isEmpty()
				&& (freeAudioNodes.isEmpty() && totalAudioWorkersCount < priorityAudioTasks.size())) {
			freeAudioNodes.add(nodeManager.getFreeNodes().get(0));
			audioNodes.add(nodeManager.getFreeNodes().get(0));
		}

		Iterator<ClientAudioTask> taskIt = priorityAudioTasks.iterator();
		for (Node node : freeAudioNodes) {
			if (!taskIt.hasNext()) {
				break;
			}
			ClientAudioTask next = taskIt.next();
			Codec taskCodec = next.getStreamConfig().getOutStream().getStrategy().getCodec();

			if (node.getCodecs().contains(taskCodec)) {
				dispatch(next, node);
				taskIt.remove();
			} else {

			}
		}

		for (Node node : nodeManager.getFreeNodes()) {
			ClientVideoTask task = getNextVideoTask(node.getCodecs());
			if (task != null && node.getAllTasks().size() == 0) {
				dispatch(task, node);
				break;
			}
		}
	}

	private void dispatch(ClientTask task, Node node) {
		if (assign(task, node)) {
			dispatcherPool.add(new DispatchItem(new TaskRequestMessage(task), node));
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
			Assignment assignment = new Assignment(task, node);

			assignments.put(task, assignment);
			task.assign();
			node.addPendingTask(task);
			node.lock();
			assigned = true;
			logger.fine(String.format("Assigned %s to node %s.%n", task, node.getName()));
		} else {
			logger.warning(String.format("Could not assign %s to node %s.%n", task, node.getName()));
		}

		return assigned;
	}

	private void confirm(ClientTask task) {
		task.start();
		Assignment assignment = assignments.get(task);
		assignment.getAssignee().confirm(task);
	}

	/**
	 * Unassign a node from a task.
	 *
	 * @param task
	 *            The task to be unassigned.
	 * @return True if task could be unassigned
	 */
	private synchronized boolean unassign(ClientTask task) {
		boolean unassigned = false;
		Assignment assignment = this.assignments.remove(task);

		if (assignment != null && assignment.getAssignee() != null) {
			Node previousAssignee = assignment.getAssignee();
			unassigned = true;
			previousAssignee.removeTask(task);

			logger.fine(String.format("Node %s  was unassigned from %s.%n", previousAssignee.getName(), task.toString()));
		}
		return unassigned;
	}

	public boolean taskUpdated(ClientTask task, Node node) {
		TaskState updateStatus = task.getProgress().getTaskState();

		switch (updateStatus) {
		case TASK_COMPLETED:
			logger.fine(String.format("Worker %s completed %s.%n", node.getName(), task));

			Job job = this.jobs.get(task.getJobId());
			task.completed();

			if (job.getTaskDoneCount() == job.getTaskCount()) {
				logger.fine(String.format("Job %s completed.%n", job.getJobId()));
				listener.handle(new Event(EventEnum.JOB_ENCODING_COMPLETED, job));
			}

			unassign(task);
			break;
		case TASK_CANCELED:
			unassign(task);
			task.getProgress().reset();
			break;
		case TASK_COMPUTING:
		case TASK_ASSIGNED:
			Job dispatched = this.getJob(task.getJobId());
			if (!dispatched.isStarted()) {
				dispatched.start();
			}
			break;
		case TASK_FAILED:
			unassign(task);
			task.getProgress().reset();
			node.failure(); // Add a failure count to the node
			break;
		case TASK_TODO:
			break;
		}
		return false;
	}

	public Job getJob(String jobId) {
		return jobs.get(jobId);
	}

	/**
	 * Remove completed jobs from job list.
	 */
	public void cleanJobs() {
		ArrayList<Job> toClean = new ArrayList<>();

		for (Job job : getJobs()) {
			if (job.isCompleted()) {
				toClean.add(job);
			}
		}

		for (Job job : toClean) {
			deleteJob(job);
		}
	}

	@Override
	public void handle(Event event) {
		switch (event.getCode()) {
		case DISPATCH_ITEM_REFUSED:
			DispatchItem item = (DispatchItem) event.getObject();
			ClientTask task = ((TaskRequestMessage) item.getMessage()).getTask();

			unassign(task);
			break;
		case NODE_DISCONNECTED:
			Node disconnectedNode = (Node) event.getObject();

			unassingAll(disconnectedNode);
			break;
		case TASK_CONFIRMED:
			ClientTask confirmedTask = (ClientTask) event.getObject();
			ClientTask masterInstance = getTask(confirmedTask.getJobId(), confirmedTask.getTaskId());

			confirm(masterInstance);
			break;
		case TASK_REFUSED:
			ClientTask refusedTask = (ClientTask) event.getObject();

			taskRefused(getTask(refusedTask.getJobId(), refusedTask.getTaskId()));
			break;
		default:
			break;
		}
	}

	private void taskRefused(ClientTask task) {
		logger.fine(String.format("A worker refused %s !%n", task));

		task.reset();
		unassign(task);
	}

	/**
	 * Unassign all tasks of the node.
	 *
	 * @param n
	 *            The node to remove all tasks
	 */
	public void unassingAll(Node n) {
		ArrayList<ClientTask> tasks = n.getAllTasks();
		for (ClientTask clientTask : tasks) {
			unassign(clientTask);
		}
		n.removeAllTasks();
	}

	public ClientTask getTask(Job job, int taskId) {
		ClientTask instance = null;
		for (ClientTask task : job.getClientTasks()) {
			if (task.getTaskId() == taskId) {
				instance = task;
				break;
			}
		}
		return instance;
	}

	public ClientTask getTask(String jobId, int taskId) {
		return getTask(getJob(jobId), taskId);
	}

	public ArrayList<Assignment> getAssignments(Node assignee) {
		ArrayList<Assignment> nodeAssigments = new ArrayList<>();

		for (Assignment assignment : this.assignments.values()) {
			if (assignment.getAssignee().equals(assignee)) {
				nodeAssigments.add(assignment);
			}
		}

		return nodeAssigments;
	}

	private boolean isInDelay(Assignment assignment) {
		long delay = System.currentTimeMillis() - assignment.getTime();

		return delay >= DISPATCH_DELAY_MSEC;
	}

	public synchronized void removeInvalidAssigments(Node sender, ArrayList<ClientTask> reportTasks) {
		for (Assignment assignment : getAssignments(sender)) {
			if (!reportTasks.contains(assignment.getTask()) && isInDelay(assignment)) {

				logger.finer(String.format("Removed task %d of job %s from worker %s has it was in an invalid state.%n",
						assignment.getTask().getTaskId(), assignment.getTask().getJobId(), assignment.getAssignee()
								.getName()));

				sender.removeTask(assignment.getTask());
				assignments.remove(assignment.getTask(), assignment);
			}
		}
	}

	private void removeLastInstanceInProgressTasks() {
		ArrayList<TaskState> inProgressStates = new ArrayList<>();
		inProgressStates.addAll(Arrays.asList(new TaskState[] { TaskState.TASK_ASSIGNED, TaskState.TASK_COMPUTING }));

		for (Job j : this.jobs.values()) {
			for (ClientTask task : j.getClientTasks()) {
				if (inProgressStates.contains(task.getProgress().getTaskState())) {
					task.reset();
				}
			}
		}
	}

	public boolean handleJobRequest(ApiJobRequest req) {
		return jobInitiator.process(req);
	}

}
