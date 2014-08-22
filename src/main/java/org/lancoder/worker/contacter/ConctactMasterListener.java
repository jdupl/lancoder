package org.lancoder.worker.contacter;

import java.net.InetAddress;

public interface ConctactMasterListener {

	public void receivedUnid(String unid);

	public String getCurrentNodeUnid();
	
	public String getCurrentNodeName();

	public InetAddress getCurrentNodeAddress();

	public int getCurrentNodePort();

}
