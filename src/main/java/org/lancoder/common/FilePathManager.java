package org.lancoder.common;

import java.io.File;

import org.lancoder.common.config.Config;
import org.lancoder.common.job.Job;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.utils.FileUtils;

public class FilePathManager {

	private Config config;

	public FilePathManager(Config config) {
		this.config = config;
	}

	public File getSharedFinalFile(Job job) {
		return FileUtils.getFile(config.getAbsoluteSharedFolder(), job.getOutputFolder(), job.getOutputFileName());
	}

	public File getSharedPartsFolder(Job job) {
		return FileUtils.getFile(config.getAbsoluteSharedFolder(), job.getPartsFolderName());
	}

	public File getSharedFinalFile(ClientTask task) {
		return FileUtils.getFile(config.getAbsoluteSharedFolder(), task.getFinalFile().getPath());
	}

	public File getLocalTempFile(ClientTask task) {
		return FileUtils.getFile(config.getTempEncodingFolder(), task.getTempFile().getPath());
	}

	public File getLocalTempFolder(ClientTask task) {
		return FileUtils.getFile(config.getTempEncodingFolder(), task.getJobId(), String.valueOf(task.getTaskId()));
	}

	public File getSharedSourceFile(Job job) {
		return FileUtils.getFile(config.getAbsoluteSharedFolder(), job.getSourceFile());
	}

	public File getSharedSourceFile(ClientTask task) {
		return FileUtils.getFile(config.getAbsoluteSharedFolder(), task.getStreamConfig().getOrignalStream()
				.getRelativeFile());
	}

}
