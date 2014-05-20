package drfoliberg.worker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import drfoliberg.common.Service;
import drfoliberg.common.exceptions.MissingDecoderException;
import drfoliberg.common.exceptions.MissingFfmpegException;
import drfoliberg.common.network.Cause;
import drfoliberg.common.network.messages.CrashReport;
import drfoliberg.common.status.NodeState;
import drfoliberg.common.task.Task;

public class WorkThread extends Service {

	private InetAddress masterIp;
	private Task task;
	private Worker callback;
	Process process;

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

	@Override
	public void run() {
		try {
			System.out.println("WORKER WORK THREAD: Executing a task!");
			// use start and duration for ffmpeg legacy support
			long durationMs = task.getEncodingEndTime() - task.getEncodingStartTime();
			String startTimeStr = getDurationString(task.getEncodingStartTime());
			String durationStr = getDurationString(durationMs);

			// Get absolute path of output file and check if it exists
			// TODO add output folder
			File absoluteSharedDir = new File(callback.config.getAbsoluteSharedFolder());
			File outputFile = new File(absoluteSharedDir, new File(
					String.format("output-part_%d.mkv", task.getTaskId())).toString());
			if (outputFile.exists()) {
				System.err.printf("File %s exists ! deleting file...\n", outputFile.getAbsoluteFile());
				if (!outputFile.delete()) {
					System.err.printf("Could not delete file %s ", outputFile.getAbsoluteFile());
					throw new IOException();
				} else {
					System.err.println("Success deleting file");
				}
			}

			// Get absolute path of input file
			File inputFile = new File(absoluteSharedDir, task.getSourceFile());

			// Get parameters from the task and bind parameters to process
			try {
				// TODO Protect from spaces in paths
				String processStr = String.format(
						"ffmpeg -ss %s -t %s -i %s -force_key_frames 0 -an -c:v %s -b:v %dk %s", startTimeStr,
						durationStr, inputFile.getAbsolutePath(), "libx264", task.getBitrate(), outputFile);
				System.out.println(processStr);
				process = Runtime.getRuntime().exec(processStr);
			} catch (IOException e) {
				// Send crash report
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
			}
			s.close();

			if (!close) {
				// process exited normally
				callback.taskDone(task, masterIp);
			} else {
				// work thread is being interrupted
				process.destroy();
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Work was interrupted!");
		} catch (MissingFfmpegException e) {
			// TODO send crash report
			CrashReport report = new CrashReport(callback.config.getUniqueID(), new Cause(e, "", true),
					callback.getStatusReport());
			callback.sendCrashReport(report);
			// update status
			callback.updateStatus(NodeState.CRASHED);
		} catch (MissingDecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
