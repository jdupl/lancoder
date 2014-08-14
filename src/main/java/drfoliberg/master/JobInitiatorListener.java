package drfoliberg.master;

import drfoliberg.common.job.Job;

public interface JobInitiatorListener {

	public void newJob(Job job);
}
