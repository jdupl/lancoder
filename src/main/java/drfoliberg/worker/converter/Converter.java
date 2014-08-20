package drfoliberg.worker.converter;

import java.io.File;
import java.io.IOException;

import drfoliberg.common.RunnableService;
import drfoliberg.common.utils.FileUtils;

public abstract class Converter extends RunnableService {

	protected File taskTempOutputFile;
	protected File taskTempOutputFolder;
	protected File taskFinalFolder;
	protected File absoluteSharedDir;

	protected ConverterListener listener;

	protected void createDirs() {
		if (!taskFinalFolder.exists()) {
			taskFinalFolder.mkdirs();
			FileUtils.givePerms(taskFinalFolder, false);
		}

		if (!taskTempOutputFolder.exists()) {
			taskTempOutputFolder.mkdirs();
			FileUtils.givePerms(taskTempOutputFolder, false);
		}
		// remove any previous temp files for this part
		cleanTempPart();
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
