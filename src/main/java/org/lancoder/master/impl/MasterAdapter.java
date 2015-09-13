package org.lancoder.master.impl;

import java.util.logging.Logger;

import org.lancoder.common.Node;
import org.lancoder.common.events.Event;
import org.lancoder.common.events.EventEnum;
import org.lancoder.common.events.EventListener;
import org.lancoder.common.job.Job;
import org.lancoder.common.network.messages.web.ApiJobRequest;
import org.lancoder.common.network.messages.web.ApiResponse;
import org.lancoder.master.JobInitiatorListener;
import org.lancoder.master.api.web.ApiHandlerListener;
import org.lancoder.muxer.MuxerListener;

public class MasterAdapter implements MuxerListener, JobInitiatorListener, EventListener, ApiHandlerListener {

	private Master master;
	private Logger logger = Logger.getLogger("lancoder");

	public MasterAdapter(Master master) {
		this.master = master;
	}

	@Override
	public void newJob(Job job) {
		this.master.getJobManager().addJob(job);
	}

	@Override
	public void jobMuxingStarted(Job job) {
		job.muxing();
	}

	@Override
	public void jobMuxingCompleted(Job job) {
		job.complete();
		logger.fine(String.format("Job %s finished muxing !\n", job.getJobName()));
	}

	@Override
	public void jobMuxingFailed(Job job) {
		job.fail();
		logger.fine(String.format("Muxing failed for job %s\n", job.getJobName()));
	}

	@Override
	public void handle(Event event) {
		this.master.handle(event);
	}

	@Override
	public void disconnectNode(String unid) {
		Node node = master.getNodeManager().identifySender(unid);
		master.handle(new Event(EventEnum.NODE_DISCONNECTED, node));
	}

	@Override
	public boolean addJob(ApiJobRequest req) {
		return master.getJobManager().handleJobRequest(req);
	}

	@Override
	public ApiResponse apiDeleteJob(String id) {
		return master.apiDeleteJob(id);
	}
}
