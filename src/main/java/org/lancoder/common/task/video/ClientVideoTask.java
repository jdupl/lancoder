package org.lancoder.common.task.video;

import java.util.ArrayList;

import org.lancoder.common.strategies.stream.VideoEncodeStrategy;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.Task;

public class ClientVideoTask extends ClientTask {

	private static final long serialVersionUID = 4705790541885491703L;

	public ClientVideoTask(Task task, VideoStreamConfig streamConfig) {
		super(task, streamConfig);
	}

	@Override
	public VideoStreamConfig getStreamConfig() {
		return (VideoStreamConfig) this.streamConfig;
	}

	public ArrayList<String> getPresetArg() {
		ArrayList<String> args = new ArrayList<>();
		VideoEncodeStrategy streamStrategy = (VideoEncodeStrategy) this.getStreamConfig().getOutStream().getStrategy();
		if (streamStrategy.getPreset() != null && streamStrategy.getCodec().supportsPresets()) {
			args.add("-preset");
			args.add(streamStrategy.getPreset().toString());
		}
		return args;
	}

	@Override
	public VideoTask getTask() {
		return (VideoTask) this.task;
	}

}
