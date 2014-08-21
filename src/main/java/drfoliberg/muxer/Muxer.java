package drfoliberg.muxer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import drfoliberg.common.RunnableService;
import drfoliberg.common.file_components.streams.Stream;
import drfoliberg.common.job.Job;
import drfoliberg.common.task.Task;

public class Muxer extends RunnableService {

	private ArrayList<MuxerListener> listeners = new ArrayList<>();
	private Job job;
	private String absoluteJobFolder;

	public Muxer(MuxerListener listener, Job job, String absoluteJobFolder) {
		listeners.add(listener);
		this.job = job;
		this.absoluteJobFolder = absoluteJobFolder;
	}

	@Override
	public void run() {
		boolean success = false;
		File muxOutputFile = new File(absoluteJobFolder, job.getOutputFileName());
		ArrayList<String> args = new ArrayList<>();
		Collections.addAll(args, new String[] { "mkvmerge", "-o", muxOutputFile.getAbsolutePath() });

		// Iterate through streams
		Iterator<Stream> streamIterator = job.getFileInfo().getStreams().iterator();
		while (streamIterator.hasNext()) {
			// Add stream arguments 
			args.add("--forced-track");
			args.add("0:no");
			Stream stream = streamIterator.next();
			Iterator<Task> taskIterator = job.getTasksForStream(stream).iterator();
			// Iterate through tasks of the stream and concatenate if necessary
			while (taskIterator.hasNext()) {
				Task t = taskIterator.next();
				// Add file arguments
				Collections.addAll(args, new String[] { "-d", "0", "-A", "-S", "-T", "--no-global-tags",
						"--no-chapters" });
				args.add(t.getOutputFile());
				if (taskIterator.hasNext()) {
					// Concatenate to the next task
					args.add("+");
				}
			}
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
