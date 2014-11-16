package org.lancoder.muxer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.lancoder.common.FilePathManager;
import org.lancoder.common.codecs.Codec;
import org.lancoder.common.exceptions.MissingDecoderException;
import org.lancoder.common.exceptions.MissingThirdPartyException;
import org.lancoder.common.file_components.streams.Stream;
import org.lancoder.common.job.Job;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.common.utils.FileUtils;
import org.lancoder.worker.converter.video.Transcoder;

/**
 * Implementation of the muxer module with native ffmpeg
 * 
 * @author justin
 *
 */
public class FFmpegMuxer extends Pooler<Job> {

	private MuxerListener listener;
	private FilePathManager filePathManager;
	private FFmpeg ffMpeg;
	/**
	 * Map of the input files without the duplicated. Value is the position in ffmpeg file mapping. (file:stream)
	 */
	private HashMap<String, Integer> inputs = new HashMap<>();

	public FFmpegMuxer(MuxerListener listener, FilePathManager filePathManager, FFmpeg ffMpeg) {
		super();
		this.listener = listener;
		this.filePathManager = filePathManager;
		this.ffMpeg = ffMpeg;
	}

	private int getInputIndex(String input) {
		if (!inputs.containsKey(input)) {
			inputs.put(input, inputs.size());
		}
		return inputs.get(input);
	}

	/**
	 * Build mapping of a stream. Indexes the stream index to the input file of the stream.
	 * 
	 * @param stream
	 *            The stream to map
	 * @return The map of the input file index and the local stream index
	 */
	private Map buildMap(Stream stream) {
		int streamIndex = 0;
		String input = null;

		// TODO move input file to strategy
		if (stream.getCodec() == Codec.COPY) {
			// Use original source file and stream id
			// ffmpeg -i source.mkv -map 0:streamId -c copy final.mkv
			input = filePathManager.getSharedSourceFile(task).getAbsolutePath();
			streamIndex = stream.getIndex();
		} else {
			// Iterate through tasks of the stream and concatenate if necessary
			ArrayList<ClientTask> tasks = task.getTasksForStream(stream);
			if (tasks.size() > 1) {
				// Register concat stream as an input stream
				// ffmpeg -i "concat:input1.mpg|input2.mpg|input3.mpg" -map 0:0 -c copy output.mpg
				StringBuilder sb = new StringBuilder("concat:");
				Iterator<ClientTask> taskIterator = tasks.iterator();
				while (taskIterator.hasNext()) {
					ClientTask t = taskIterator.next();
					File taskPartFile = filePathManager.getSharedFinalFile(t);
					sb.append(taskPartFile.getAbsolutePath());
					if (taskIterator.hasNext()) {
						// Concatenate to the next task
						sb.append("|");
					}
				}
				input = sb.toString();
				streamIndex = 0;
			} else if (tasks.size() == 1) {
				// Use temp file of the task
				// ffmpeg -i parts/part0.mkv -map 0:0 -c copy final.mkv
				File taskFile = filePathManager.getSharedFinalFile(tasks.get(0));
				input = taskFile.getAbsolutePath();
				streamIndex = 0;
			}
		}
		return new Map(getInputIndex(input), streamIndex);
	}

	/**
	 * Build maps for all streams of the current job. Also indexes input files.
	 * 
	 * @return All stream mappings
	 */
	private ArrayList<Map> buildMapping() {
		// Iterate through original streams
		ArrayList<Map> mapping = new ArrayList<>();
		Iterator<Stream> streamIterator = task.getStreams().iterator();
		while (streamIterator.hasNext()) {
			mapping.add(buildMap(streamIterator.next()));
		}
		return mapping;
	}

	@Override
	protected void start() {
		boolean success = false;
		ArrayList<String> args = new ArrayList<>();

		File muxOutputFile = filePathManager.getSharedFinalFile(task);
		ArrayList<Map> mapping = buildMapping();

		args.add(ffMpeg.getPath());
		// Add input files
		for (String input : inputs.keySet()) {
			// TODO check if order is reliable
			args.add("-i");
			args.add(input);
		}
		// Add mapping
		for (Map map : mapping) {
			args.add("-map");
			args.add(map.toString());
		}
		// Ensure that all streams are copied
		args.add("-c");
		args.add("copy");

		// Set output file
		args.add(muxOutputFile.getAbsolutePath());

		// Start the transcoding
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
		// TODO Auto-generated method stub

	}
}
