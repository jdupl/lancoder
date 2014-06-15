package drfoliberg.worker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import drfoliberg.common.Service;
import drfoliberg.common.exceptions.MissingDecoderException;
import drfoliberg.common.exceptions.MissingFfmpegException;
import drfoliberg.common.exceptions.WorkInterruptedException;
import drfoliberg.common.network.Cause;
import drfoliberg.common.network.messages.cluster.CrashReport;
import drfoliberg.common.status.NodeState;
import drfoliberg.common.task.Task;

public class WorkThread extends Service {

	private InetAddress masterIp;
	private Task task;
	private Worker callback;
	Process process;

	File jobFinalFolder;
	File taskFinalFolder;

	File absoluteSharedDir;
	File taskTempOutputFile;
	File taskTempOutputFolder;

	public WorkThread(Worker w, Task t, InetAddress masterIp) {
		this.masterIp = masterIp;
		task = t;
		callback = w;
		callback.getCurrentTask().start();
	}

	/**
	 * Convert ms count to hh:mm:ss.xxx format
	 * 
	 * @param ms
	 *            The ms count to convert
	 * @return The string in the right format for ffmpeg/libav
	 */
	private String getDurationString(long ms) {
		int hours = (int) (ms / (3600 * 1000));
		int remaining = (int) (ms - hours * 3600 * 1000);
		int minutes = (int) (remaining / (60 * 1000));

		remaining -= minutes * 60 * 1000;

		int seconds = remaining / 1000;
		int decimals = remaining % 1000;
		return String.format("%d:%d:%d.%d", hours, minutes, seconds, decimals);
	}

	public void encodePass(String startTimeStr, String durationStr) throws MissingFfmpegException,
			MissingDecoderException, WorkInterruptedException {
		task.setTimeStarted(System.currentTimeMillis());
		File inputFile = new File(absoluteSharedDir, task.getSourceFile());
		// Get parameters from the task and bind parameters to process
		try {
			String[] baseArgs = new String[] { "ffmpeg", "-ss", startTimeStr, "-t", durationStr, "-i",
					inputFile.getAbsolutePath(), "-force_key_frames", "0", "-an", "-c:v", "libx264" };
			ArrayList<String> ffmpegArgs = new ArrayList<>();
			// Add base args to process builder
			for (String arg : baseArgs) {
				ffmpegArgs.add(arg);
			}

			ffmpegArgs.addAll(task.getRateControlArgs());
			ffmpegArgs.addAll(task.getPresetArg());

			// Add output file
			String outFile = taskTempOutputFile.getAbsoluteFile().toString();
			if (task.getPasses() > 1) {
				ffmpegArgs.add("-pass");
				ffmpegArgs.add(String.valueOf(task.getTaskStatus().getCurrentPass()));
				if (task.getTaskStatus().getCurrentPass() != task.getPasses()) {
					ffmpegArgs.add("-f");
					ffmpegArgs.add("rawvideo");
					ffmpegArgs.add("-y");
					outFile = "/dev/null"; // TODO use NUL for windows
				}
			}
			ffmpegArgs.add(outFile);

			ProcessBuilder pb = new ProcessBuilder(ffmpegArgs);
			System.err.println(pb.command().toString());
			pb.directory(taskTempOutputFolder);
			process = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
			throw new MissingFfmpegException();
		}

		// Read from ffmpeg stderr to get progress
		InputStream stderr = process.getErrorStream();
		Scanner s = new Scanner(stderr);
		String line = "";

		Pattern currentFramePattern = Pattern.compile("frame=\\s*([0-9]*)");
		Pattern fpsPattern = Pattern.compile("fps=\\s*([0-9]*)");
		Pattern missingDecoder = Pattern.compile("Error while opening encoder for output stream");
		while (s.hasNext() && !close) {
			// TODO better scanning (avoid regexing the same line multiple times if result)
			try {
				line = s.nextLine();
				Matcher m = currentFramePattern.matcher(line);
				if (m.find()) {
					long currentFrame = Long.parseLong(m.group(1));
					callback.getCurrentTask().setFramesCompleted(currentFrame);

					System.err.printf("frame: %d out of %d (%f%%) \n", currentFrame, task.getEstimatedFramesCount(),
							callback.getCurrentTask().getProgress());
				}
				m = fpsPattern.matcher(line);
				if (m.find()) {
					float fps = Float.parseFloat(m.group(1));
					callback.getCurrentTask().setFps(fps);
					System.err.printf("fps: %s \n", fps);
				}
				m = missingDecoder.matcher(line);
				if (m.find()) {
					s.close();
					System.err.println("Missing decoder !");
					throw new MissingDecoderException();
				}
			} catch (NullPointerException e) {
				// If task is interrupted, current task might become null
				if (!close) {
					// If thread is not stopped and a null pointer occurs, it is not normal
					throw new WorkInterruptedException();
				}
			}
		}
		s.close();

		if (close) {
			process.destroy();
			throw new WorkInterruptedException();
		}
	}

	@Override
	public void run() {
		try {
			System.out.println("WORKER WORK THREAD: Executing a task!");
			// use start and duration for ffmpeg legacy support
			long durationMs = task.getEncodingEndTime() - task.getEncodingStartTime();
			String startTimeStr = getDurationString(task.getEncodingStartTime());
			String durationStr = getDurationString(durationMs);

			createDirs();

			task.getTaskStatus().setCurrentPass((byte) 1);
			while (task.getTaskStatus().getCurrentPass() <= task.getPasses()) {
				System.err.printf("Encoding pass %d of %d\n", task.getTaskStatus().getCurrentPass(), task.getPasses());
				encodePass(startTimeStr, durationStr);
				task.getTaskStatus().setCurrentPass((byte) (task.getTaskStatus().getCurrentPass() + 1));
			}

			moveTempPartFile();
			cleanTempPart();
			callback.taskDone(task, masterIp);

		} catch (MissingFfmpegException e) {
			// TODO send crash report
			CrashReport report = new CrashReport(callback.config.getUniqueID(), new Cause(e, "", true),
					callback.getStatusReport());
			callback.sendCrashReport(report);
			// update status
			callback.updateStatus(NodeState.CRASHED);
		} catch (MissingDecoderException e) {
			e.printStackTrace();
		} catch (WorkInterruptedException e) {
			System.err.println("WORKER: stopping work");
			cleanTempPart();
		}
	}

	private void cleanTempPart() {
		System.out.println("WORKER: Deleting temp task folder");
		if (taskTempOutputFolder.exists()) {
			try {
				FileUtils.deleteDirectory(taskTempOutputFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void moveTempPartFile() {
		// TODO check if file already exists at destination and delete ?
		System.out.println("WORKER: Moving temp file to shared folder");
		try {
			FileUtils.moveFileToDirectory(taskTempOutputFile, taskFinalFolder, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createDirs() {
		absoluteSharedDir = new File(callback.config.getAbsoluteSharedFolder());
		// Create final part folder
		jobFinalFolder = new File(absoluteSharedDir, callback.config.getFinalEncodingFolder());
		jobFinalFolder = new File(jobFinalFolder, this.task.getJobId());

		taskFinalFolder = new File(jobFinalFolder, "parts");
		// taskFinalFolder = new File(taskFinalFolder, String.valueOf(task.getTaskId()));

		// Create temp part folder
		String tempOutput = callback.config.getTempEncodingFolder();

		File tempOutputJob = new File(new File(tempOutput), task.getJobId());
		if (!tempOutputJob.exists()) {
			tempOutputJob.mkdirs();
		}

		taskTempOutputFolder = new File(tempOutputJob, "parts");
		taskTempOutputFolder = new File(taskTempOutputFolder, String.format("part_%d", task.getTaskId()));

		// remove any previous temp files for this part
		cleanTempPart();
		taskTempOutputFolder.mkdirs();

		taskTempOutputFile = new File(taskTempOutputFolder, String.format("output-part_%d.mkv", task.getTaskId()));
	}
}
