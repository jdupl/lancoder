package org.lancoder.worker.converter;

import java.io.File;
import java.io.IOException;

import org.lancoder.common.RunnableService;
import org.lancoder.common.utils.FileUtils;
import org.lancoder.ffmpeg.FFmpegReaderListener;

public abstract class Converter extends RunnableService implements FFmpegReaderListener {

	protected File taskTempOutputFile;
	protected File taskTempOutputFolder;
	protected File taskFinalFolder;
	protected File absoluteSharedDir;
	protected File taskFinalFile;

	protected ConverterListener listener;

	protected void createDirs() {
		if (!taskFinalFolder.exists()) {
			taskFinalFolder.mkdirs();
			FileUtils.givePerms(taskFinalFolder, false);
		}
		if (!taskTempOutputFolder.exists()) {
			// remove any previous temp files for this part
			taskTempOutputFolder.mkdirs();
			FileUtils.givePerms(taskTempOutputFolder, false);
		}
	}

	protected void cleanTempPart() {
		System.out.println("WORKER: Deleting temp task folder content.");
		if (taskTempOutputFolder.exists()) {
			try {
				FileUtils.cleanDirectory(taskTempOutputFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
