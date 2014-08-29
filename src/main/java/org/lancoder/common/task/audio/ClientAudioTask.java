package org.lancoder.common.task.audio;

import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.Task;

public class ClientAudioTask extends ClientTask {

	private static final long serialVersionUID = 5247269871999772761L;

	public ClientAudioTask(Task prototypeTaskConfig, AudioStreamConfig prototypeStreamConfig) {
		super(prototypeTaskConfig, prototypeStreamConfig);
	}

	@Override
	public AudioStreamConfig getStreamConfig() {
		return (AudioStreamConfig) this.streamConfig;
	}

	@Override
	public AudioTask getTask() {
		return (AudioTask) this.task;
	}

}
