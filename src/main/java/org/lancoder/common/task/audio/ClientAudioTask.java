package org.lancoder.common.task.audio;

import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.Task;

public class ClientAudioTask extends ClientTask {

	private static final long serialVersionUID = 5247269871999772761L;

	private String tempFile;

	public ClientAudioTask(Task prototypeTaskConfig, AudioStreamConfig prototypeStreamConfig, String tempFile) {
		super(prototypeTaskConfig, prototypeStreamConfig);
		this.tempFile = tempFile;
	}

	public String getTempFile() {
		return tempFile;
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
