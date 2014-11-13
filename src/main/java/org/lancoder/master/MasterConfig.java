package org.lancoder.master;

import java.io.File;

import org.lancoder.common.annotations.Prompt;
import org.lancoder.common.config.Config;

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
	private static final String DEFAULT_MKV_MERGE_PATH = "mkvmerge";

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
	@Prompt(message = "MkvMerge's path")
	private String mkvMergePath;

	private String savedInstancePath = new File(System.getProperty("user.home"),
			".local/share/lancoder/master_instance.bin").getPath();

	public MasterConfig() {
		nodeServerPort = DEFAULT_NODE_LISTEN_PORT;
		finalEncodingFolder = DEFAULT_ENCODE_DESTINATION;
		absoluteSharedFolder = DEFAULT_ABSOLUTE_PATH;
		apiServerPort = DEFAULT_API_LISTEN_PORT;
		ffmpegPath = DEFAULT_FFMPEG_PATH;
		ffprobePath = DEFAULT_FFPROBE_PATH;
		mkvMergePath = DEFAULT_MKV_MERGE_PATH;
	}

	public String getSavedInstancePath() {
		return savedInstancePath;
	}

	public String getMkvMergePath() {
		return mkvMergePath;
	}

	public String getFFprobePath() {
		return ffprobePath;
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
