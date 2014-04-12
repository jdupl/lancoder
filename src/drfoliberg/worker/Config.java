package drfoliberg.worker;

import java.net.InetAddress;

public class Config {
	private InetAddress masterIpAddress;
	private int masterPort;
	private int listenPort;
	private String uniqueID;
	private String name;

	public Config(InetAddress masterIpAddress, int masterPort, int listenPort,
			String uniqueID, String name) {
		super();
		this.masterIpAddress = masterIpAddress;
		this.masterPort = masterPort;
		this.listenPort = listenPort;
		this.uniqueID = uniqueID;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
		//TODO write configuration to disk
	}

	public InetAddress getMasterIpAddress() {
		return masterIpAddress;
	}

	public void setMasterIpAddress(InetAddress masterIpAddress) {
		this.masterIpAddress = masterIpAddress;
	}

	public int getMasterPort() {
		return masterPort;
	}

	public void setMasterPort(int masterPort) {
		this.masterPort = masterPort;
	}

	public int getListenPort() {
		return listenPort;
	}

	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}

}
