package drfoliberg.worker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

public class Config {

	private InetAddress masterIpAddress;
	private int masterPort;
	private int listenPort;
	private String uniqueID;
	private String name;
	private String absoluteSharedFolder;
	private String tempEncodingFolder;
	private String finalEncodingFolder;

	public Config(InetAddress masterIpAddress, int masterPort, int listenPort, String uniqueID, String name) {
		this.masterIpAddress = masterIpAddress;
		this.masterPort = masterPort;
		this.listenPort = listenPort;
		this.uniqueID = uniqueID;
		this.name = name;
	}

	public synchronized boolean dump(File f) {
		Gson gson = new Gson();
		String s = gson.toJson(this);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
			fos.write(s.getBytes("UTF-8"));
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO handle errors

		return true;
	}

	public Config load(File f) {
		Config config = null;
		if (!f.exists()) {
			return null;
		}
		try {
			byte[] b = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
			Gson gson = new Gson();
			config = gson.fromJson(new String(b, "UTF-8"), Config.class);
		} catch (IOException e) {
			// TODO handle errors
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
		this.dump(new File(Worker.CONFIG_PATH));
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
