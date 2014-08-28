package org.lancoder.common.task.audio;

import org.lancoder.common.progress.Unit;
import org.lancoder.common.task.Task;

public class AudioTask extends Task {

	private static final long serialVersionUID = -3534097948858145557L;

	public AudioTask(int taskId, String jobId, int stepCount, long encodingStartTime, long encodingEndTime,
			long unitCount, Unit unit) {
		super(taskId, jobId, stepCount, encodingStartTime, encodingEndTime, unitCount, unit);
	}

}
