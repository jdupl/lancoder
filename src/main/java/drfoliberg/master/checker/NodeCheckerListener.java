package drfoliberg.master.checker;

import java.util.ArrayList;

import drfoliberg.common.Node;
import drfoliberg.common.network.messages.cluster.StatusReport;
import drfoliberg.common.task.video.TaskReport;

public interface NodeCheckerListener {

	public ArrayList<Node> getNodes();

	public ArrayList<Node> getOnlineNodes();
	
	public void nodeDisconnected(Node n);

	public void readTaskReport(TaskReport taskReport);

	public void readStatusReport(StatusReport statusReport);
}
