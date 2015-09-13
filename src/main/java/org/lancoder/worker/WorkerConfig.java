package org.lancoder.worker;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.lancoder.common.annotations.Prompt;
import org.lancoder.common.config.Config;

public class WorkerConfig extends Config implements Serializable {

	private static final long serialVersionUID = 4279318303054715575L;
	private static final String DEFAULT_PATH = new File(System.getProperty("user.home"),
			".config/lancoder/worker_config.conf").getPath();

	/**
	 * Defaults values of the config
	 */
	private static final int DEFAULT_MASTER_PORT = 1337;
	private static final int DEFAULT_LISTEN_PORT = 1338;
	private static final String DEFAULT_MASTER_IP = InetAddress.getLoopbackAddress().getHostAddress();
	private static final String DEFAULT_UNID = "";
	private static final String DEFAULT_NAME = InetAddress.getLoopbackAddress().getCanonicalHostName();

	@Prompt(message = "master's ip or hostname", priority = 1)
	private String masterIpAddress;

	@Prompt(message = "master's listening port", priority = 2, advanced = true)
	private int masterPort;

	@Prompt(message = "worker's name", priority = 11)
	private String name;

	@Prompt(message = "worker's listening port", priority = 12, advanced = true)
	private int listenPort;

	private String uniqueID;

	public WorkerConfig() {
		super();
		this.masterIpAddress = DEFAULT_MASTER_IP;
		this.masterPort = DEFAULT_MASTER_PORT;
		this.listenPort = DEFAULT_LISTEN_PORT;
		this.uniqueID = DEFAULT_UNID;
		this.name = DEFAULT_NAME;
	}

	/**
	 * Try to set worker name to the local hostname. Calls to this method can block for ~5 seconds on some systems. This
	 * would otherwise always be called in the constructor otherwise.
	 */
	public void setNameFromHostName() {
		Logger logger = Logger.getLogger("lancoder");
		try {
			logger.info("Trying to resolve local hostname.\n");
			this.name = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.warning("Could not resolve local hostname.\n");
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(String.format("Will be contacting master at: %s:%d.%n", this.getMasterIpAddress(),
				this.getMasterPort()));
		return sb.toString();
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
	}

	public String getMasterIpAddress() {
		return masterIpAddress;
	}

	public void setMasterIpAddress(String masterIpAddress) {
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

	@Override
	public String getDefaultPath() {
		return DEFAULT_PATH;
	}
}
