package drfoliberg.muxer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import drfoliberg.common.RunnableService;
import drfoliberg.common.job.Job;
import drfoliberg.common.task.audio.AudioEncodingTask;
import drfoliberg.common.task.video.VideoEncodingTask;

public class Muxer extends RunnableService {

	private ArrayList<MuxerListener> listeners;
	private Job job;
	private String absoluteJobFolder;

	public Muxer(MuxerListener listener, Job job, String absoluteJobFolder) {
		listeners = new ArrayList<>();
		listeners.add(listener);
		this.job = job;
		this.absoluteJobFolder = absoluteJobFolder;
	}

	@Override
	public void run() {
		boolean success = false;
		File muxedFile = new File(absoluteJobFolder, job.getOutputFileName());
		ArrayList<String> args = new ArrayList<>();
		Collections.addAll(args, new String[] { "mkvmerge", "-o", muxedFile.getAbsolutePath() });
		// Video
		args.add("--forced-track");
		args.add("0:no");
		for (int i = 0; i < job.getVideoTasks().size(); i++) {
			Collections.addAll(args, new String[] { "-d", "0", "-A", "-S", "-T", "--no-global-tags", "--no-chapters" });
			if (i != 0) {
				args.add("+");
			}
			VideoEncodingTask t = job.getVideoTasks().get(i);
			File path = new File(t.getOutputFile());
			path = new File(job.getPartsFolderName(), path.getName());
			args.add(path.getPath());
		}
		// Audio
		for (int i = 0; i < job.getAudioTasks().size(); i++) {
			Collections.addAll(args, new String[] { "--forced-track", "0:no", "-a", "0", "-D", "-S", "-T",
					"--no-global-tags", "--no-chapters" });
			AudioEncodingTask task = job.getAudioTasks().get(i);
			args.add(task.getOutputFile());
		}

		ProcessBuilder pb = new ProcessBuilder(args);
		System.out.println("MUXER: " + args.toString());
		pb.directory(new File(absoluteJobFolder));
		System.out.println("MUXER: trying to mux in path " + absoluteJobFolder);
		fireMuxingStarting();

		try {
			Process m = pb.start();
			m.waitFor();
			success = m.exitValue() == 0 ? true : false;
		} catch (IOException e) {
			fireMuxingFailed(e);
		} catch (InterruptedException e) {
			fireMuxingFailed(e);
		} finally {
			if (success) {
				fireMuxingCompleted();
			} else {
				fireMuxingFailed();
			}
		}
	}

	private void fireMuxingStarting() {
		for (MuxerListener l : this.listeners) {
			l.muxingStarting(job);
		}
	}

	private void fireMuxingCompleted() {
		for (MuxerListener l : this.listeners) {
			l.muxingCompleted(job);
		}
	}

	private void fireMuxingFailed(Exception e) {
		for (MuxerListener l : this.listeners) {
			l.muxingFailed(job, e);
		}
	}

	private void fireMuxingFailed() {
		Exception e = new Exception("Unknown error");
		for (MuxerListener l : this.listeners) {
			l.muxingFailed(job, e);
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		if (job != null) {
			fireMuxingFailed(e);
		}
	}

}
