package org.lancoder.master.dispatcher;

import org.lancoder.common.Node;
import org.lancoder.common.events.Event;
import org.lancoder.common.events.EventEnum;
import org.lancoder.common.events.EventListener;
import org.lancoder.common.network.cluster.messages.TaskRequestMessage;
import org.lancoder.common.pool.Pool;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.ClientTask;

public class DispatcherPool extends Pool<DispatchItem> implements DispatcherListener {

	private static final int MAX_DISPATCHERS = 5;

	private EventListener listener;

	public DispatcherPool(EventListener listener) {
		super(MAX_DISPATCHERS);
		this.listener = listener;
	}

	@Override
	protected Pooler<DispatchItem> getPoolerInstance() {
		return new Dispatcher(this);
	}

	@Override
	public synchronized void taskRefused(DispatchItem item) {
		ClientTask t = ((TaskRequestMessage) item.getMessage()).getTask();
		Node n = item.getNode();
		System.err.printf("Node %s refused task.%n", n.getName());
		t.getProgress().reset();
		if (n.hasTask(t)) {
			n.getCurrentTasks().remove(t);
		}
		n.unlock();
		listener.handle(new Event(EventEnum.WORK_NEEDS_UPDATE));
	}

	@Override
	public synchronized void taskAccepted(DispatchItem item) {
		ClientTask t = ((TaskRequestMessage) item.getMessage()).getTask();
		Node n = item.getNode();
		n.unlock();
		System.out.printf("Node %s accepted task %d from %s.%n", n.getName(), t.getTaskId(), t.getJobId());
		listener.handle(new Event(EventEnum.WORK_NEEDS_UPDATE));
	}

}
