package drfoliberg.worker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import drfoliberg.common.Status;
import drfoliberg.common.exceptions.MissingDecoderException;
import drfoliberg.common.exceptions.MissingFfmpegException;
import drfoliberg.common.network.Cause;
import drfoliberg.common.network.messages.CrashReport;
import drfoliberg.common.task.EncodingTask;

public class WorkThread extends Thread {

	private InetAddress masterIp;
	private EncodingTask task;
	private Worker callback;

	public WorkThread(Worker w, EncodingTask t, InetAddress masterIp) {
		this.masterIp = masterIp;
		task = t;
		callback = w;
		callback.setCurrentTaskStatus(new CurrentTaskStatus(t.getEstimatedFrameCount()));
	}

	/**
	 * Convert ms count to hh:mm:ss.xxx format
	 * 
	 * @param ms
	 *            The ms count to counvert
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
			// use start and duration for legacy support
			long durationMs = task.getEndTime() - task.getStartTime();
			String startTimeStr = getDurationString(task.getStartTime());
			String durationStr = getDurationString(durationMs);

			String outputFile = String.format("/home/justin/encoding/output-part_%d.mkv", task.getTaskId());

			// ffmpeg -i ~/encoding/input.mkv -c:v libx264 -b:v 1000k -strict -2
			// ~/encoding/output.mkv

			System.out.println("WORKER WORK THREAD: Executing a task!");
			File f = new File(outputFile);
			if (f.exists()) {
				System.err.printf("File %s exists ! deleting file...\n", f.getAbsoluteFile());
				if (!f.delete()) {
					System.err.printf("Could not delete file %s ", f.getAbsoluteFile());
					throw new IOException();
				} else {
					System.err.println("Success deleting file");
				}
			}

			// Get parameters from the task and bind parameters to process
			Process process = null;

			try {
				// TODO Protect from spaces in paths
				String processStr = String.format(
						"ffmpeg -ss %s -t %s -i %s -force_key_frames 0 -an -c:v %s -b:v %s %s", startTimeStr,
						durationStr, "/home/justin/encoding/input.mkv", "libx264", "1000k", outputFile);
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
			while (s.hasNext()) {
				// TODO better scanning (avoid regexing the same line multiple times if result)
				line = s.nextLine();

				Matcher m = currentFramePattern.matcher(line);
				if (m.find()) {
					long currentFrame = Long.parseLong(m.group(1));
					callback.getCurrentTaskStatus().setFramesDone(currentFrame);

					System.err.printf("frame: %d out of %d (%f%%) \n", currentFrame, task.getEstimatedFrameCount(),
							callback.getCurrentTaskStatus().getProgress());
				}
				m = fpsPattern.matcher(line);
				if (m.find()) {
					float fps = Float.parseFloat(m.group(1));
					callback.getCurrentTaskStatus().setFps(fps);
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
			callback.taskDone(task, masterIp);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Work was interrupted!");
		} catch (MissingFfmpegException e) {
			// TODO send crash report
			CrashReport report = new CrashReport(callback.config.getUniqueID(), new Cause(e, "", true),
					callback.getStatusReport());
			callback.sendCrashReport(report);
			// update status
			callback.updateStatus(Status.CRASHED);
		} catch (MissingDecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
