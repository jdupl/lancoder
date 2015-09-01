package org.lancoder.muxer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.third_parties.MkvMerge;
import org.lancoder.common.third_parties.ThirdParty;
import org.lancoder.common.utils.FileUtils;
import org.lancoder.worker.converter.video.Transcoder;

public class MkvMergeMuxer extends Muxer {

	private MuxerListener listener;
	private FilePathManager filePathManager;
	private MkvMerge mkvMerge;
	/**
	 * Map of the input files without the duplicated. Value is the position in file mapping. (file:stream)
	 */
	private HashMap<String, Input> inputs = new HashMap<>();
	private Job job;

	public MkvMergeMuxer(MuxerListener listener, FilePathManager filePathManager, MkvMerge mkvMerge) {
		super();
		this.listener = listener;
		this.filePathManager = filePathManager;
		this.mkvMerge = mkvMerge;
	}

	public ArrayList<String> getArgs() {
		ArrayList<String> args = new ArrayList<>();
		ArrayList<Stream> audioCopyStreams = new ArrayList<>();
		ArrayList<Stream> videoCopyStreams = new ArrayList<>();
		File muxOutputFile = filePathManager.getSharedFinalFile(job);

		args.add(mkvMerge.getPath());

		// Set output file
		args.add("-o");
		args.add(muxOutputFile.getPath());

		ArrayList<Stream> sortedStreams = new ArrayList<>(job.getStreams());
		Collections.sort(sortedStreams);

		for (Stream stream : sortedStreams) {
			if (stream.getStrategy().isCopy()) {
				if (stream instanceof AudioStream) {
					audioCopyStreams.add(stream);
				} else if (stream instanceof VideoStream) {
					videoCopyStreams.add(stream);
				}
			} else {
				args.addAll(buildArgsForEncodedStream(stream));
			}
		}

		args.addAll(buildArgsForCopyStreams(audioCopyStreams));
		args.addAll(buildArgsForCopyStreams(videoCopyStreams));

		return args;
	}

	private ArrayList<String> buildArgsForCopyStreams(ArrayList<Stream> streams) {
		ArrayList<String> args = new ArrayList<>();

		// Build copy streams args '-a|v 1,2,3 original.mkv'
		if (streams.size() > 0) {
			args.add("-" + streams.get(0).getMkvMergeStreamTypeArg());

			StringBuilder sb = new StringBuilder();
			for (Iterator<Stream> iterator = streams.iterator(); iterator.hasNext();) {
				sb.append(iterator.next().getIndex());

				if (iterator.hasNext()) {
					sb.append(',');
				}
			}

			args.add(sb.toString());
			args.add(filePathManager.getSharedSourceFile(job).getAbsolutePath());
		}

		return args;
	}

	private ArrayList<String> buildArgsForEncodedStream(Stream stream) {
		ArrayList<String> args = new ArrayList<>();

		// Iterate through tasks of the stream and concatenate if necessary
		ArrayList<ClientTask> tasks = job.getTasksForStream(stream);

		Iterator<ClientTask> it = tasks.iterator();
		while (it.hasNext()) {
			args.add(filePathManager.getSharedFinalFile(it.next()).getAbsolutePath());

			if (it.hasNext()) {
				args.add("+");
			}
		}

		return args;
	}

	@Override
	public void handle(Job job) {
		this.job = job;
		boolean success = false;
		ArrayList<String> args = getArgs();

		// Start the transcoding
		this.listener.jobMuxingStarted(job);

		Transcoder transcoder = new Transcoder();
		try {
			success = transcoder.read(args);
		} catch (MissingDecoderException | MissingThirdPartyException e) {
			e.printStackTrace();
		} finally {
			inputs.clear();
			if (success) {
				File partsDirectory = filePathManager.getSharedPartsFolder(job);
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
				this.listener.jobMuxingCompleted(job);
			} else {
				this.listener.jobMuxingFailed(job);
			}
			this.job = null;
		}
	}

	@Override
	public ThirdParty getMuxingThirdParty() {
		return this.mkvMerge;
	}
}
