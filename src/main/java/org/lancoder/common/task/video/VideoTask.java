package org.lancoder.common.task.video;

import org.lancoder.common.progress.Unit;
import org.lancoder.common.task.Task;

public class VideoTask extends Task {

	private static final long serialVersionUID = 3834075993276994157L;

	public VideoTask(int taskId, String jobId, int stepCount, long encodingStartTime, long encodingEndTime,
			long unitCount, Unit unit) {
		super(taskId, jobId, stepCount, encodingStartTime, encodingEndTime, unitCount, unit);
	}

}
