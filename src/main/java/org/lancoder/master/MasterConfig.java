package org.lancoder.master;

import java.io.File;
import java.util.ArrayList;

import org.lancoder.common.Node;
import org.lancoder.common.annotations.Prompt;
import org.lancoder.common.config.Config;
import org.lancoder.common.job.Job;

public class MasterConfig extends Config {

	private final static String DEFAULT_PATH = new File(System.getProperty("user.home"),
			".config/lancoder/master_config.json").getPath();
	/**
	 * Defaults values of the config
	 */
	private static final int DEFAULT_NODE_LISTEN_PORT = 1337;
	private static final int DEFAULT_API_LISTEN_PORT = 8080;
	private static final String DEFAULT_ENCODE_DESTINATION = "encodes";
	private static final String DEFAULT_ABSOLUTE_PATH = System.getProperty("user.home");
	private static final String DEFAULT_FFPROBE_PATH = "ffprobe";

	@Prompt(message = "master's listening port")
	private int nodeServerPort;
	@Prompt(message = "webui port")
	private int apiServerPort;
	@Prompt(message = "shared folder root")
	private String absoluteSharedFolder;
	@Prompt(message = "output directory (relative to shared folder)")
	private String finalEncodingFolder;
	@Prompt(message = "FFmpeg's path")
	private String ffmpegPath;
	@Prompt(message = "FFprobe's path")
	private String ffprobePath;

	private ArrayList<Node> nodeList = new ArrayList<>();
	public ArrayList<Job> jobList = new ArrayList<>();

	public MasterConfig() {
		nodeServerPort = DEFAULT_NODE_LISTEN_PORT;
		finalEncodingFolder = DEFAULT_ENCODE_DESTINATION;
		absoluteSharedFolder = DEFAULT_ABSOLUTE_PATH;
		apiServerPort = DEFAULT_API_LISTEN_PORT;
		ffmpegPath = DEFAULT_FFMPEG_PATH;
		ffprobePath = DEFAULT_FFPROBE_PATH;
	}

	public String getFFprobePath() {
		return ffprobePath;
	}

	public ArrayList<Job> getJobList() {
		return jobList;
	}

	public void setJobList(ArrayList<Job> jobList) {
		this.jobList = jobList;
	}

	public ArrayList<Node> getNodeList() {
		return nodeList;
	}

	public void setNodeList(ArrayList<Node> nodeList) {
		this.nodeList = nodeList;
	}

	public String getAbsoluteSharedFolder() {
		return absoluteSharedFolder;
	}

	public void setAbsoluteSharedFolder(String absoluteSharedFolder) {
		this.absoluteSharedFolder = absoluteSharedFolder;
	}

	public String getFinalEncodingFolder() {
		return finalEncodingFolder;
	}

	public void setFinalEncodingFolder(String finalEncodingFolder) {
		this.finalEncodingFolder = finalEncodingFolder;
	}

	public int getNodeServerPort() {
		return nodeServerPort;
	}

	public void setNodeServerPort(int nodeServerPort) {
		this.nodeServerPort = nodeServerPort;
	}

	public int getApiServerPort() {
		return apiServerPort;
	}

	public void setApiServerPort(int apiServerPort) {
		this.apiServerPort = apiServerPort;
	}

	@Override
	public String getDefaultPath() {
		return DEFAULT_PATH;
	}

	@Override
	public String getFFmpegPath() {
		return ffmpegPath;
	}
}
