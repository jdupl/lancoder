package drfoliberg.worker;

import drfoliberg.common.network.Cause;
import drfoliberg.common.task.video.VideoEncodingTask;

public interface WorkThreadListener {

	public void workStarted(VideoEncodingTask task);

	public void workCompleted(VideoEncodingTask task);

	public void workFailed(VideoEncodingTask task);

	public void nodeCrash(Cause cause);

	public WorkerConfig getConfig();
}