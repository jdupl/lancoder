package drfoliberg.worker;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

public class WorkerConfig implements Serializable {

	private static final long serialVersionUID = 4279318303054715575L;

	private static final String WORKER_CONFIG_PATH = "worker_config.json";

	private static final transient int DEFAULT_MASTER_PORT = 1337;
	private static final int DEFAULT_LISTEN_PORT = 1338;
	private static final InetAddress DEFAULT_MASTER_IP = InetAddress.getLoopbackAddress();
	private static final String DEFAULT_ENCODE_DIRECTORY = "encodes";
	private static final String DEFAULT_TEMP_DIRECTORY = "/tmp";
	private static final String DEFAULT_UNID = "";
	private static final String DEFAULT_NAME = "";
	private static final String DEFAULT_ABSOLUTE_PATH = "~";

	private InetAddress masterIpAddress;
	private int masterPort;
	private int listenPort;
	private String uniqueID;
	private String name;
	private String absoluteSharedFolder;
	private String tempEncodingFolder;
	private String finalEncodingFolder;

	public WorkerConfig() {
		this.masterIpAddress = DEFAULT_MASTER_IP;
		this.masterPort = DEFAULT_MASTER_PORT;
		this.listenPort = DEFAULT_LISTEN_PORT;
		this.uniqueID = DEFAULT_UNID;
		this.name = DEFAULT_NAME;
		this.absoluteSharedFolder = DEFAULT_ABSOLUTE_PATH;
		this.tempEncodingFolder = DEFAULT_TEMP_DIRECTORY;
		this.finalEncodingFolder = DEFAULT_ENCODE_DIRECTORY;
	}

	/**
	 * Generate default config and save to disk.
	 * 
	 * @return The default config
	 */
	public static WorkerConfig generate() {
		WorkerConfig conf = new WorkerConfig();
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
			Files.write(Paths.get(WORKER_CONFIG_PATH), s.getBytes("UTF-8"));
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
	public static WorkerConfig load() {
		WorkerConfig config = null;
		if (!Files.exists(Paths.get(WORKER_CONFIG_PATH))) {
			return null;
		}
		try {
			byte[] b = Files.readAllBytes(Paths.get(WORKER_CONFIG_PATH));
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

	public String getFinalEncodingFolder() {
		return finalEncodingFolder;
	}

	public void setFinalEncodingFolder(String finalEncodingFolder) {
		this.finalEncodingFolder = finalEncodingFolder;
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
		this.dump();
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
