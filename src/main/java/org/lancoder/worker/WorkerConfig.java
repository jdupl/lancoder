package org.lancoder.worker;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;

import org.lancoder.common.annotations.Prompt;
import org.lancoder.common.config.Config;

public class WorkerConfig extends Config implements Serializable {

	private static final long serialVersionUID = 4279318303054715575L;

	private final static String DEFAULT_PATH = new File(System.getProperty("user.home"),
			".config/lancoder/worker_config.json").getPath();

	/**
	 * Defaults values of the config
	 */
	private static final int DEFAULT_MASTER_PORT = 1337;
	private static final int DEFAULT_LISTEN_PORT = 1338;
	private static final String DEFAULT_MASTER_IP = InetAddress.getLoopbackAddress().getHostAddress();
	private static final String DEFAULT_TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
	private static final String DEFAULT_UNID = "";
	private static final String DEFAULT_NAME = InetAddress.getLoopbackAddress().getCanonicalHostName();
	private static final String DEFAULT_ABSOLUTE_PATH = System.getProperty("user.home");

	@Prompt(message = "master ip address")
	private String masterIpAddress;
	@Prompt(message = "master port")
	private int masterPort;
	@Prompt(message = "worker port")
	private int listenPort;
	private String uniqueID;
	@Prompt(message = "worker's name")
	private String name;
	@Prompt(message = "shared folder root")
	private String absoluteSharedFolder;
	@Prompt(message = "temporary files location")
	private String tempEncodingFolder;
	@Prompt(message = "FFmpeg's path")
	public static String ffmpegPath;

	public WorkerConfig() {
		this.masterIpAddress = DEFAULT_MASTER_IP;
		this.masterPort = DEFAULT_MASTER_PORT;
		this.listenPort = DEFAULT_LISTEN_PORT;
		this.uniqueID = DEFAULT_UNID;
		this.name = DEFAULT_NAME;
		this.absoluteSharedFolder = DEFAULT_ABSOLUTE_PATH;
		this.tempEncodingFolder = DEFAULT_TEMP_DIRECTORY;
		WorkerConfig.ffmpegPath = DEFAULT_FFMPEG_PATH;
	}

	public String getAbsoluteSharedFolder() {
		return absoluteSharedFolder;
	}

	public void setAbsoluteSharedFolder(String absoluteSharedFolder) {
		this.absoluteSharedFolder = absoluteSharedFolder;
	}

	public String getTempEncodingFolder() {
		return tempEncodingFolder;
	}

	public void setTempEncodingFolder(String tempEncodingFolder) {
		this.tempEncodingFolder = tempEncodingFolder;
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
