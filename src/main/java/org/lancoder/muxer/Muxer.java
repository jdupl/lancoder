package org.lancoder.muxer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.lancoder.common.FilePathManager;
import org.lancoder.common.codecs.Codec;
import org.lancoder.common.exceptions.MissingDecoderException;
import org.lancoder.common.exceptions.MissingThirdPartyException;
import org.lancoder.common.file_components.streams.Stream;
import org.lancoder.common.job.Job;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.utils.FileUtils;
import org.lancoder.worker.converter.video.Transcoder;

public class Muxer extends Pooler<Job> {

	private MuxerListener listener;
	private FilePathManager filePathManager;

	public Muxer(MuxerListener listener, FilePathManager filePathManager) {
		super();
		this.listener = listener;
		this.filePathManager = filePathManager;
	}

	@Override
	protected void start() {
		boolean success = false;
		File muxOutputFile = filePathManager.getSharedFinalFile(task);
		ArrayList<String> args = new ArrayList<>();
		Collections.addAll(args, new String[] { "mkvmerge", "-o", muxOutputFile.getAbsolutePath() });
		// Iterate through original streams
		Iterator<Stream> streamIterator = task.getStreams().iterator();
		while (streamIterator.hasNext()) {
			// Add stream arguments
			args.add("--forced-track");
			args.add("0:no");
			Stream stream = streamIterator.next();
			if (stream.getCodec() == Codec.COPY) {
				File streamOrigin = filePathManager.getSharedSourceFile(task);
				args.addAll(stream.getStreamCopyMapping());
				args.add(streamOrigin.getPath());
			} else {
				ArrayList<ClientTask> tasks = task.getTasksForStream(stream);
				// Iterate through tasks of the stream and concatenate if necessary
				Iterator<ClientTask> taskIterator = tasks.iterator();
				while (taskIterator.hasNext()) {
					ClientTask t = taskIterator.next();
					File partFile = filePathManager.getSharedFinalFile(t);
					args.add(partFile.getAbsolutePath());
					if (taskIterator.hasNext()) {
						// Concatenate to the next task
						args.add("+");
					}
				}
			}
		}
		this.listener.jobMuxingStarted(task);
		Transcoder transcoder = new Transcoder();
		try {
			success = transcoder.read(args);
		} catch (MissingDecoderException | MissingThirdPartyException e) {
			serviceFailure(e);
		} finally {
			if (success) {
				File partsDirectory = filePathManager.getSharedPartsFolder(task);
				try {
					FileUtils.deleteDirectory(partsDirectory);
				} catch (IOException e) {
					System.err.println("Error while deleting parts of job");
					e.printStackTrace();
				}
				this.listener.jobMuxingCompleted(task);
			} else {
				this.listener.jobMuxingFailed(task);
			}
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		e.printStackTrace();
		this.listener.jobMuxingFailed(task);
	}

}
