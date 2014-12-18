package org.lancoder.common.task.video;

import java.util.ArrayList;

import org.lancoder.common.file_components.streams.VideoStream;
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
		VideoStream stream = this.getStreamConfig().getOutStream();
		if (stream.getPreset() != null && stream.getCodec().supportsPresets()) {
			args.add("-preset");
			args.add(stream.getPreset().toString());
		}
		return args;
	}

	@Override
	public VideoTask getTask() {
		return (VideoTask) this.task;
	}

}
