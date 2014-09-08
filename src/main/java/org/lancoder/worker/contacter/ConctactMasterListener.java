package org.lancoder.worker.contacter;

import java.net.InetAddress;
import java.util.ArrayList;

import org.lancoder.common.codecs.Codec;

public interface ConctactMasterListener {

	public void receivedUnid(String unid);

	public String getCurrentNodeUnid();

	public String getCurrentNodeName();

	public ArrayList<Codec> getAvailableCodecs();

	public InetAddress getCurrentNodeAddress();

	public int getCurrentNodePort();

	public int getThreadCount();

}
