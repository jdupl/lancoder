package drfoliberg.master;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.google.gson.Gson;

import drfoliberg.common.Node;
import drfoliberg.common.task.Job;

public class MasterConfig {

	public static final String MASTER_CONFIG_PATH = "master_config.json";

	/**
	 * Defaults values of the config
	 */
	private static final int DEFAULT_LISTEN_PORT = 1337;
	private static final String DEFAULT_ENCODE_DESTINATION = "encodes";
	// maybe change for winblows support
	private static final String DEFAULT_ABSOLUTE_PATH = "~";

	private int listenPort;
	private String absoluteSharedFolder;
	private String finalEncodingFolder;

	private ArrayList<Node> nodeList;
	public ArrayList<Job> jobList;

	public MasterConfig() {
		listenPort = DEFAULT_LISTEN_PORT;
		finalEncodingFolder = DEFAULT_ENCODE_DESTINATION;
		absoluteSharedFolder = DEFAULT_ABSOLUTE_PATH;

		jobList = new ArrayList<Job>();
		nodeList = new ArrayList<Node>();
	}

	/**
	 * Generate default config and save to disk.
	 * 
	 * @return The default config
	 */
	public static MasterConfig generate() {
		MasterConfig conf = new MasterConfig();
		conf.dump();
		return conf;
	}

	/**
	 * Serializes current config to disk as JSON object.
	 * 
	 * @return True if could write config to disk. Otherwise, return false.
	 */
	public synchronized boolean dump() {
		Gson gson = new Gson();
		String s = gson.toJson(this);

		try {
			Files.write(Paths.get(MASTER_CONFIG_PATH), s.getBytes("UTF-8"));
		} catch (IOException e) {
			// print stack and return false
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public synchronized static MasterConfig load() {
		MasterConfig config = null;
		
		if (!Files.exists(Paths.get(MASTER_CONFIG_PATH))) {
			return null;
		}
		
		try {
			byte[] b = Files.readAllBytes(Paths.get(MASTER_CONFIG_PATH));
			Gson gson = new Gson();
			config = gson.fromJson(new String(b, "UTF-8"), MasterConfig.class);
		} catch (IOException e) {
			// print stack and return null
			e.printStackTrace();
		}
		return config;
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

	public int getListenPort() {
		return listenPort;
	}

	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}

}
