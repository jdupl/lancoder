package org.lancoder.common.task;

public class ClientAudioTask extends ClientTask {

	private static final long serialVersionUID = 5247269871999772761L;

	public ClientAudioTask(PrototypeTask prototypeTaskConfig, AudioStreamConfig prototypeStreamConfig) {
		super(prototypeTaskConfig, prototypeStreamConfig);
	}

	@Override
	public AudioStreamConfig getStreamConfig() {
		return (AudioStreamConfig) this.streamConfig;
	}

}
