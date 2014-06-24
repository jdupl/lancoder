package main.java.drfoliberg.worker;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WorkerConfig implements Serializable {

	private static final long serialVersionUID = 4279318303054715575L;

	// private static final String WORKER_CONFIG_PATH = "worker_config.json";

	private static final transient int DEFAULT_MASTER_PORT = 1337;
	private static final int DEFAULT_LISTEN_PORT = 1338;
	private static final InetAddress DEFAULT_MASTER_IP = InetAddress.getLoopbackAddress();
	private static final String DEFAULT_TEMP_DIRECTORY = "/tmp";
	private static final String DEFAULT_UNID = "";
	private static final String DEFAULT_NAME = "";
	private static final String DEFAULT_ABSOLUTE_PATH = System.getProperty("user.home");

	private InetAddress masterIpAddress;
	private int masterPort;
	private int listenPort;
	private String uniqueID;
	private String name;
	private String absoluteSharedFolder;
	private String tempEncodingFolder;

	public WorkerConfig() {
		this.masterIpAddress = DEFAULT_MASTER_IP;
		this.masterPort = DEFAULT_MASTER_PORT;
		this.listenPort = DEFAULT_LISTEN_PORT;
		this.uniqueID = DEFAULT_UNID;
		this.name = DEFAULT_NAME;
		this.absoluteSharedFolder = DEFAULT_ABSOLUTE_PATH;
		this.tempEncodingFolder = DEFAULT_TEMP_DIRECTORY;
	}

	/**
	 * Generate default config and save to disk.
	 * 
	 * @return The default config
	 */
	public static WorkerConfig generate(String configPath) {
		WorkerConfig conf = new WorkerConfig();
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

	/**
	 * Loads json config (editable by user) from disk
	 * 
	 * @return The config or null if no file was readable
	 */
	public static WorkerConfig load(String configPath) {
		WorkerConfig config = null;
		if (!Files.exists(Paths.get(configPath))) {
			return null;
		}
		try {
			byte[] b = Files.readAllBytes(Paths.get(configPath));
			Gson gson = new Gson();
			config = gson.fromJson(new String(b, "UTF-8"), WorkerConfig.class);
		} catch (IOException e) {
			// print stack and return null
			e.printStackTrace();
		}
		return config;
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
