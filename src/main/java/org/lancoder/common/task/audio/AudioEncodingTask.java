package org.lancoder.common.task.audio;

import org.lancoder.common.file_components.streams.AudioStream;
import org.lancoder.common.progress.Unit;
import org.lancoder.common.task.StreamConfig;
import org.lancoder.common.task.PrototypeTask;

public class AudioEncodingTask extends PrototypeTask {

	private static final long serialVersionUID = 1319651638856267785L;
	private static final Unit DEFAULT_UNIT = Unit.SECONDS;

	public AudioEncodingTask(int taskId, int stepCount, long encodingStartTime, long encodingEndTime, long unitCount,
			AudioStream stream, StreamConfig config) {
		super(taskId, stepCount, encodingStartTime, encodingEndTime, unitCount, DEFAULT_UNIT);
	}
}
