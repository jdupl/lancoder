package main.java.drfoliberg.master;

import main.java.drfoliberg.common.Node;
import main.java.drfoliberg.common.task.video.VideoEncodingTask;

public interface DispatcherListener {

	public void taskRefused(VideoEncodingTask t, Node n);

	public void taskAccepted(VideoEncodingTask t, Node n);

}
