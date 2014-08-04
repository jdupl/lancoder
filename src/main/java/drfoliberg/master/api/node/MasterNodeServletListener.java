package drfoliberg.master.api.node;

import drfoliberg.common.ServletListener;
import drfoliberg.common.network.messages.cluster.ConnectMessage;
import drfoliberg.common.network.messages.cluster.StatusReport;

public interface MasterNodeServletListener extends ServletListener {

	public String connectRequest(ConnectMessage cm);

	public void disconnectRequest(ConnectMessage cm);

	public void readStatusReport(StatusReport report);

}
