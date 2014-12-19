package org.lancoder.common.strategies.stream;

import java.io.Serializable;
import java.util.ArrayList;

import org.lancoder.common.job.Job;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.StreamConfig;

public abstract class StreamHandlingStrategy implements Serializable {

	private static final long serialVersionUID = -2050092926317106808L;

	public abstract ArrayList<String> getRateControlArgs();

	public boolean isCopy() {
		return false;
	}

	public abstract ArrayList<ClientTask> createTasks(Job job, StreamConfig config);

}
