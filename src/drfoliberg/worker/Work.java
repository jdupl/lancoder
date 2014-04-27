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
import drfoliberg.common.task.Task;

public class Work extends Thread {

	private InetAddress masterIp;
	private Task task;
	private Worker callback;

	public Work(Worker w, Task t, InetAddress masterIp) {
		this.masterIp = masterIp;
		task = t;
		callback = w;
		callback.setCurrentTaskStatus(new CurrentTaskStatus(t.getEstimatedFrameCount()));
	}

	@Override
	public void run() {
		try {
			// ffmpeg -i ~/encoding/input.mkv -c:v libx264 -b:v 1000k -strict -2
			// ~/encoding/output.mkv
			System.out.println("WORKER WORK THREAD: Executing a task!");
			File f = new File("/home/justin/encoding/output.mkv");
			if (f.exists()) {
				System.err.printf("File %s exists ! deleting file...\n", f.getAbsoluteFile());
				if (!f.delete()) {
					System.err.printf("Could not delete file %s ", f.getAbsoluteFile());
					throw new IOException();
				} else {
					System.err.println("Success deleting file");
				}
			}
			// TODO Get parameters from the task and bind parameters to process

			Process process = null;
			try {
				process = Runtime.getRuntime().exec(
						"avconv -i " + "/home/justin/encoding/input.mkv -c:v " + "libx264 -b:v 1000k " + "-strict -2 "
								+ "/home/justin/encoding/output.mkv");
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
			System.out.println("Scanner closed");
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
