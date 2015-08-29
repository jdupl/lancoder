package org.lancoder.master;

import java.io.File;
import java.io.Serializable;

import org.lancoder.common.annotations.Prompt;
import org.lancoder.common.config.Config;

public class MasterConfig extends Config implements Serializable {

	private static final long serialVersionUID = 8016312645409446998L;
	private static final String DEFAULT_PATH = new File(System.getProperty("user.home"),
			".config/lancoder/master_config.conf").getPath();
	/**
	 * Defaults values of the config
	 */
	private static final int DEFAULT_NODE_LISTEN_PORT = 1337;
	private static final int DEFAULT_API_LISTEN_PORT = 8080;
	private static final String DEFAULT_ENCODE_DESTINATION = "encodes";
	private static final String DEFAULT_FFPROBE_PATH = "ffprobe";
	private static final String DEFAULT_MKVMERGE_PATH = "mkvmerge";

	@Prompt(message = "output directory (relative to shared folder)", priority = 11)
	private String finalEncodingFolder;

	@Prompt(message = "FFprobe's path", priority = 21)
	private String ffprobePath;

	@Prompt(message = "mkvmerge's path (for x265 muxing)", priority = 22)
	protected String mkvMergePath;

	@Prompt(message = "master's listening port", priority = 40, advanced = true)
	private int nodeServerPort;

	@Prompt(message = "webui port", priority = 50, advanced = true)
	private int apiServerPort;

	private String savedInstancePath = new File(System.getProperty("user.home"),
			".local/share/lancoder/master_instance.bin").getPath();

	public MasterConfig() {
		super();
		nodeServerPort = DEFAULT_NODE_LISTEN_PORT;
		finalEncodingFolder = DEFAULT_ENCODE_DESTINATION;
		apiServerPort = DEFAULT_API_LISTEN_PORT;
		ffprobePath = DEFAULT_FFPROBE_PATH;
		mkvMergePath = DEFAULT_MKVMERGE_PATH;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(String.format("Serving Webui on port: %d.%n", this.getApiServerPort()));
		sb.append(String.format("Master listening for nodes on port: %d.%n", this.getNodeServerPort()));
		return sb.toString();
	}

	public String getSavedInstancePath() {
		return savedInstancePath;
	}

	public String getFFprobePath() {
		return ffprobePath;
	}

	public String getFinalEncodingFolder() {
		return finalEncodingFolder;
	}

	public int getNodeServerPort() {
		return nodeServerPort;
	}

	public int getApiServerPort() {
		return apiServerPort;
	}

	public void setNodeServerPort(int nodeServerPort) {
		this.nodeServerPort = nodeServerPort;
	}

	public void setApiServerPort(int apiServerPort) {
		this.apiServerPort = apiServerPort;
	}

	public void setFinalEncodingFolder(String finalEncodingFolder) {
		this.finalEncodingFolder = finalEncodingFolder;
	}

	public void setFfprobePath(String ffprobePath) {
		this.ffprobePath = ffprobePath;
	}

	@Override
	public String getDefaultPath() {
		return DEFAULT_PATH;
	}

	public String getMkvMergePath() {
		return mkvMergePath;
	}

	public String getFfprobePath() {
		return ffprobePath;
	}

	public void setMkvMergePath(String mkvMergePath) {
		this.mkvMergePath = mkvMergePath;
	}

}
