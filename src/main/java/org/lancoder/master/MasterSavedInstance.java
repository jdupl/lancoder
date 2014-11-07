package org.lancoder.master;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import org.lancoder.common.Node;
import org.lancoder.common.job.Job;

/**
 * Class used to serialize objects of master to reload and save states.
 * 
 */
public class MasterSavedInstance implements Serializable {

	private static final long serialVersionUID = 8032984414587133704L;

	private HashMap<String, Node> nodes = new HashMap<>();
	private HashMap<String, Job> jobs = new HashMap<>();

	public MasterSavedInstance(HashMap<String, Node> nodes, HashMap<String, Job> jobs) {
		this.nodes = nodes;
		this.jobs = jobs;
	}

	public static void save(File file, MasterSavedInstance current) {
		System.err.println("saving to " + file.getAbsolutePath());
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		try (FileOutputStream out = new FileOutputStream(file); ObjectOutputStream oos = new ObjectOutputStream(out)) {
			oos.writeObject(current);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load last state instance of master
	 * 
	 * @param file
	 *            The serialized file
	 * @return The old instance
	 */
	public static MasterSavedInstance load(File file) {
		MasterSavedInstance instance = null;
		if (file.exists()) {
			try (FileInputStream in = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(in)) {
				instance = (MasterSavedInstance) ois.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	public HashMap<String, Node> getNodes() {
		return nodes;
	}

	public void setNodes(HashMap<String, Node> nodes) {
		this.nodes = nodes;
	}

	public HashMap<String, Job> getJobs() {
		return jobs;
	}

	public void setJobs(HashMap<String, Job> jobs) {
		this.jobs = jobs;
	}

}
