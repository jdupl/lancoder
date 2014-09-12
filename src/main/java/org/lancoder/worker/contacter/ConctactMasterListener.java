package org.lancoder.worker.contacter;

import org.lancoder.common.network.messages.cluster.ConnectMessage;

public interface ConctactMasterListener {

	public ConnectMessage getConnectMessage();

	public void receivedUnid(String unid);

}
