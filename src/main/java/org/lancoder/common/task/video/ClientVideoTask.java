package org.lancoder.common.task.video;

import java.util.ArrayList;

import org.lancoder.common.codecs.CodecFactory;
import org.lancoder.common.codecs.base.AbstractCodec;
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

	public ArrayList<String> getRateControlArgs() {
		ArrayList<String> args = new ArrayList<>();
		VideoStream stream = this.getStreamConfig().getOutStream();
		AbstractCodec codecInstance = CodecFactory.fromCodec(stream.getCodec());
		switch (stream.getRateControlType()) {
		case VBR:
			args.add(codecInstance.getVBRSwitchArg());
			args.add(String.format("%dk", stream.getRate()));
			break;
		case CRF:
			args.add(codecInstance.getCRFSwitchArg());
			args.add(String.format("%d", stream.getRate()));
			break;
		default:
			// TODO throw exception
			break;
		}
		return args;
	}

	public ArrayList<String> getPresetArg() {
		ArrayList<String> args = new ArrayList<>();
		VideoStream stream = this.getStreamConfig().getOutStream();
		AbstractCodec codecInstance = CodecFactory.fromCodec(stream.getCodec());
		if (stream.getPreset() != null && codecInstance.supportsPresets()) {
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
