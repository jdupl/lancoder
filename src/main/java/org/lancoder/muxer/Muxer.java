package org.lancoder.muxer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.lancoder.common.RunnableService;
import org.lancoder.common.codecs.Codec;
import org.lancoder.common.file_components.streams.Stream;
import org.lancoder.common.job.Job;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.utils.FileUtils;

public class Muxer extends RunnableService {

	private MuxerListener listener;
	private Job job;

	public Muxer(MuxerListener listener, Job job) {
		this.listener = listener;
		this.job = job;
	}

	@Override
	public void run() {
		boolean success = false;
		File muxOutputFile = FileUtils.getFile(listener.getSharedFolder(), job.getOutputFolder(),
				job.getOutputFileName());
		ArrayList<String> args = new ArrayList<>();
		Collections.addAll(args, new String[] { "mkvmerge", "-o", muxOutputFile.getAbsolutePath() });
		// Iterate through original streams
		Iterator<Stream> streamIterator = job.getStreams().iterator();
		while (streamIterator.hasNext()) {
			// Add stream arguments
			args.add("--forced-track");
			args.add("0:no");
			Stream stream = streamIterator.next();
			if (stream.getCodec() == Codec.COPY) {
				File streamOrigin = new File(listener.getSharedFolder(), stream.getRelativeFile());
				args.addAll(stream.getStreamCopyMapping());
				args.add(streamOrigin.getPath());
			} else {
				ArrayList<ClientTask> tasks = job.getTasksForStream(stream);
				// Iterate through tasks of the stream and concatenate if necessary
				Iterator<ClientTask> taskIterator = tasks.iterator();
				while (taskIterator.hasNext()) {
					ClientTask t = taskIterator.next();
					File partFile = FileUtils.getFile(listener.getSharedFolder(), t.getTempFile());
					args.add(partFile.getAbsolutePath());
					if (taskIterator.hasNext()) {
						// Concatenate to the next task
						args.add("+");
					}
				}
			}
		}
		ProcessBuilder pb = new ProcessBuilder(args);
		System.out.println("MUXER: " + args.toString());
		listener.muxingStarting(job);
		try {
			Process m = pb.start();
			m.waitFor();
			success = m.exitValue() == 0 ? true : false;
		} catch (IOException | InterruptedException e) {
			serviceFailure(e);
		} finally {
			if (success) {
				listener.muxingCompleted(job);
			}
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		if (job != null) {
			listener.muxingFailed(job, e);
		}
	}
}
