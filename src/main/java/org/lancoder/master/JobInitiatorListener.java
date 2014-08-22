package org.lancoder.master;

import org.lancoder.common.job.Job;

public interface JobInitiatorListener {

	public void newJob(Job job);
}
