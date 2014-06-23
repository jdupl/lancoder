package drfoliberg.master;

import drfoliberg.common.Node;
import drfoliberg.common.task.video.VideoEncodingTask;

public interface DispatcherListener {

	public void taskRefused(VideoEncodingTask t, Node n);

	public void taskAccepted(VideoEncodingTask t, Node n);

}
