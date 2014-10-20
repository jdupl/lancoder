package org.lancoder.worker.converter;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.lancoder.common.config.Config;
import org.lancoder.common.pool.PoolListener;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.utils.FileUtils;
import org.lancoder.ffmpeg.FFmpegReaderListener;

public abstract class Converter<T extends ClientTask> extends Pooler<T> implements FFmpegReaderListener {

	protected String absoluteSharedFolderStr;
	protected String tempEncodingFolderStr;

	/**
	 * /tmp/jobId/
	 */
	protected File jobTempOutputFolder;
	/**
	 * /tmp/jobId/taskId/
	 */
	protected File taskTempOutputFolder;
	/**
	 * /tmp/jobId/taskId/filename
	 */
	protected File taskTempOutputFile;
	protected File taskFinalFolder;
	protected File absoluteSharedDir;
	protected File taskFinalFile;
	protected Config config;

	/**
	 * Constructor of base converter. Initialize file names and directories from task configuration.
	 * 
	 * @param task
	 *            The ClientTask containing global task config.
	 */
	public Converter(PoolListener<T> listener, String absoluteSharedFolder, String tempEncodingFolder, Config config) {
		super(listener);
		this.absoluteSharedFolderStr = absoluteSharedFolder;
		this.tempEncodingFolderStr = tempEncodingFolder;
		this.config = config;
	}

	protected void setFiles() {
		absoluteSharedDir = new File(absoluteSharedFolderStr);
		jobTempOutputFolder = new File(tempEncodingFolderStr, task.getJobId());
		taskTempOutputFolder = FileUtils.getFile(jobTempOutputFolder, String.valueOf(task.getTaskId()));
		String filename = FilenameUtils.getName(task.getTempFile());
		taskTempOutputFile = new File(taskTempOutputFolder, filename);
		taskFinalFile = FileUtils.getFile(absoluteSharedDir, task.getTempFile());
		taskFinalFolder = new File(taskFinalFile.getParent());
	}

	/**
	 * Create or clean task's and job's folders.
	 */
	protected void createDirs() {
		// Create task folder on absolute share
		if (!taskFinalFolder.exists()) {
			taskFinalFolder.mkdirs();
			FileUtils.givePerms(taskFinalFolder, false);
		}
		// Create temporary task folder on local file system (also creates job's folder)
		if (!taskTempOutputFolder.exists()) {
			taskTempOutputFolder.mkdirs();
			FileUtils.givePerms(taskTempOutputFolder, false);
		} else {
			// Remove any previous temporary files for this part (on local FS)
			cleanTempFolder();
		}
	}

	/**
	 * Clean task's temporary folder.
	 */
	private void cleanTempFolder() {
		System.out.println("WORKER: Cleaning temp task folder content.");
		if (taskTempOutputFolder.isDirectory()) {
			try {
				FileUtils.cleanDirectory(taskTempOutputFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Destroy task's temporary folder and job's (parent) temporary folder if empty.
	 */
	protected void destroyTempFolder() {
		cleanTempFolder();
		System.out.printf("WORKER: Destroying temp task folder %s.\n", taskTempOutputFolder);
		taskTempOutputFolder.delete();
		if (jobTempOutputFolder.list().length == 0) {
			System.out.printf("Deleting temporary job folder %s", jobTempOutputFolder);
			jobTempOutputFolder.delete();
		} else {
			// Another worker must be using the same folder.
			System.err.printf("Job temporary folder %s is not empty. Skipping job temp folder cleaning.\n",
					jobTempOutputFolder);
		}
	}
}
