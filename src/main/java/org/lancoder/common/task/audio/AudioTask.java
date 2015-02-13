package org.lancoder.common.task.audio;

import org.lancoder.common.task.Task;
import org.lancoder.common.task.Unit;

public class AudioTask extends Task {

	private static final long serialVersionUID = -3534097948858145557L;

	public AudioTask(int taskId, String jobId, long encodingStartTime, long encodingEndTime,
			long unitCount, Unit unit, String tempFile) {
		super(taskId, jobId, 1, encodingStartTime, encodingEndTime, unitCount, unit, tempFile);
	}

}