package org.lancoder.common.strategies.stream;

import java.util.ArrayList;

import org.lancoder.common.codecs.base.Codec;
import org.lancoder.common.job.Job;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.StreamConfig;

public class CopyStrategy extends StreamHandlingStrategy {

	private static final long serialVersionUID = -3804413668867598267L;

	@Override
	public ArrayList<String> getRateControlArgs() {
		return new ArrayList<>();
	}

	@Override
	public boolean isCopy() {
		return true;
	}

	@Override
	public ArrayList<ClientTask> createTasks(Job job, StreamConfig config) {
		return new ArrayList<>();
	}

	@Override
	public Codec getCodec() {
		return null;
	}

}
