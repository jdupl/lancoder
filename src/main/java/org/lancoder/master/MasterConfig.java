package org.lancoder.master;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.lancoder.common.Node;
import org.lancoder.common.job.Job;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MasterConfig {

	/**
	 * Defaults values of the config
	 */
	private static final int DEFAULT_NODE_LISTEN_PORT = 1337;
	private static final int DEFAULT_API_LISTEN_PORT = 8080;
	private static final String DEFAULT_ENCODE_DESTINATION = "encodes";
	// maybe change for winblows support
	private static final String DEFAULT_ABSOLUTE_PATH = System.getProperty("user.home");

	private int nodeServerPort;
	private int apiServerPort;
	private String absoluteSharedFolder;
	private String finalEncodingFolder;

	private ArrayList<Node> nodeList;
	public ArrayList<Job> jobList;

	public MasterConfig() {
		nodeServerPort = DEFAULT_NODE_LISTEN_PORT;
		finalEncodingFolder = DEFAULT_ENCODE_DESTINATION;
		absoluteSharedFolder = DEFAULT_ABSOLUTE_PATH;
		apiServerPort = DEFAULT_API_LISTEN_PORT;

		jobList = new ArrayList<Job>();
		nodeList = new ArrayList<Node>();
	}

	/**
	 * Generate default config and save to disk.
	 * 
	 * @return The default config
	 */
	public static MasterConfig generate(String configPath) {
		MasterConfig conf = new MasterConfig();
		conf.dump(configPath);
		return conf;
	}

	/**
	 * Serializes current config to disk as JSON object.
	 * 
	 * @return True if could write config to disk. Otherwise, return false.
	 */
	public synchronized boolean dump(String configPath) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String s = gson.toJson(this);

		try {
			Files.write(Paths.get(configPath), s.getBytes("UTF-8"));
		} catch (IOException e) {
			// print stack and return false
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public synchronized static MasterConfig load(String configPath) {
		MasterConfig config = null;
		
		if (!Files.exists(Paths.get(configPath))) {
			return null;
		}
		
		try {
			byte[] b = Files.readAllBytes(Paths.get(configPath));
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
}
