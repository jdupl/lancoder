package org.lancoder.muxer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.lancoder.common.FilePathManager;
import org.lancoder.common.exceptions.MissingDecoderException;
import org.lancoder.common.exceptions.MissingThirdPartyException;
import org.lancoder.common.file_components.streams.AudioStream;
import org.lancoder.common.file_components.streams.Stream;
import org.lancoder.common.file_components.streams.VideoStream;
import org.lancoder.common.job.Job;
import org.lancoder.common.pool.PoolWorker;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.third_parties.MkvMerge;
import org.lancoder.common.utils.FileUtils;
import org.lancoder.worker.converter.video.Transcoder;

public class MKvMergeMuxer extends PoolWorker<Job> {

	private MuxerListener listener;
	private FilePathManager filePathManager;
	private MkvMerge mkvMerge;
	/**
	 * Map of the input files without the duplicated. Value is the position in file mapping. (file:stream)
	 */
	private HashMap<String, Input> inputs = new HashMap<>();

	public MKvMergeMuxer(MuxerListener listener, FilePathManager filePathManager, MkvMerge mkvMerge) {
		super();
		this.listener = listener;
		this.filePathManager = filePathManager;
		this.mkvMerge = mkvMerge;
	}

	public ArrayList<String> getArgs() {
		ArrayList<String> args = new ArrayList<>();
		ArrayList<Stream> audioCopyStreams = new ArrayList<>();
		ArrayList<Stream> videoCopyStreams = new ArrayList<>();
		File muxOutputFile = filePathManager.getSharedFinalFile(task);

		args.add(mkvMerge.getPath());

		// Set output file
		args.add("-o");
		args.add(muxOutputFile.getPath());

		for (int i = 0; i < task.getStreams().size(); i++) {
			if (task.getStreams().get(i).getStrategy().isCopy()) {

				if (task.getStreams().get(i) instanceof AudioStream) {
					audioCopyStreams.add(task.getStreams().get(i));
				} else if (task.getStreams().get(i) instanceof VideoStream) {
					videoCopyStreams.add(task.getStreams().get(i));
				}
			} else {
				args.addAll(buildArgs(task.getStreams().get(i)));
			}
		}
		addArgs(audioCopyStreams, args);
		addArgs(videoCopyStreams, args);

		return args;
	}

	private void addArgs(ArrayList<Stream> streams, ArrayList<String> args) {
		if (streams.size() > 0) {
			args.add("-" + getMkvMergeStreamTypeArg(streams.get(0)));
			StringBuilder sb = new StringBuilder();
			// Build copy streams args '-a 1,2,3 original.mkv'
			for (Iterator<Stream> iterator = streams.iterator(); iterator.hasNext();) {
				sb.append(iterator.next().getIndex());
				if (iterator.hasNext()) {
					sb.append(',');
				}
			}
			args.add(sb.toString());
			args.add(filePathManager.getSharedSourceFile(task).getAbsolutePath());
		}
	}


	private String getMkvMergeStreamTypeArg(Stream stream) {
		if (stream instanceof AudioStream) {
			return "a";
		}

		if (stream instanceof VideoStream) {
			return "v";
		}
		return null;
	}

	private ArrayList<String> buildArgs(Stream stream) {
		ArrayList<String> args = new ArrayList<>();

		if (stream.getStrategy().isCopy()) { // TODO move input file to strategy
			// Use original source file and stream id
			args.add("-" + getMkvMergeStreamTypeArg(stream));
			args.add(String.valueOf(stream.getIndex() - 1));
			args.add(filePathManager.getSharedSourceFile(task).getAbsolutePath());
		} else {
			// Iterate through tasks of the stream and concatenate if necessary
			ArrayList<ClientTask> tasks = task.getTasksForStream(stream);

			Iterator<ClientTask> it = tasks.iterator();
			while (it.hasNext()) {
				args.add(filePathManager.getSharedFinalFile(it.next()).getAbsolutePath());

				if (it.hasNext()) {
					args.add("+");
				}
			}
		}

		return args;
	}

	@Override
	protected void start() {
		boolean success = false;
		ArrayList<String> args = getArgs();

		// Start the transcoding
		this.listener.jobMuxingStarted(task);

		Transcoder transcoder = new Transcoder();
		try {
			success = transcoder.read(args);
		} catch (MissingDecoderException | MissingThirdPartyException e) {
			e.printStackTrace();
		} finally {
			inputs.clear();
			if (success) {
				File partsDirectory = filePathManager.getSharedPartsFolder(task);
				try {
					// Clean job's parts
					FileUtils.deleteDirectory(partsDirectory);
					// Clean batch's parts folder if empty
					File superPartsDirectory = partsDirectory.getParentFile();
					if (superPartsDirectory.list().length == 0) {
						superPartsDirectory.delete();
					}
				} catch (IOException e) {
					Logger logger = Logger.getLogger("lancoder");
					logger.warning(e.getMessage());
				}
				this.listener.jobMuxingCompleted(task);
			} else {
				this.listener.jobMuxingFailed(task);
			}
		}
	}
}
