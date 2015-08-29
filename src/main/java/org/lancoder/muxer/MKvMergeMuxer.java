package org.lancoder.muxer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.lancoder.common.FilePathManager;
import org.lancoder.common.exceptions.MissingDecoderException;
import org.lancoder.common.exceptions.MissingThirdPartyException;
import org.lancoder.common.job.Job;
import org.lancoder.common.pool.PoolWorker;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.common.third_parties.MkvMerge;
import org.lancoder.worker.converter.video.Transcoder;

public class MKvMergeMuxer extends PoolWorker<Job> {

	private MuxerListener listener;
	private FilePathManager filePathManager;
	private MkvMerge mkvMerge;
	/**
	 * Map of the input files without the duplicated. Value is the position in ffmpeg file mapping. (file:stream)
	 */
	private HashMap<String, Input> inputs = new HashMap<>();

	public MKvMergeMuxer(MuxerListener listener, FilePathManager filePathManager, MkvMerge mkvMerge) {
		super();
		this.listener = listener;
		this.filePathManager = filePathManager;
		this.mkvMerge = mkvMerge;
	}

	public ArrayList<String> getArgs() {
		File muxOutputFile = filePathManager.getSharedFinalFile(task);
		ArrayList<String> args = new ArrayList<>();

		args.add(mkvMerge.getPath());
		// Set output file
		args.add("-o");
		args.add(muxOutputFile.getAbsolutePath());

		Iterator<ClientVideoTask> iterator = task.getClientVideoTasks().iterator();
		while (iterator.hasNext()) {
			ClientVideoTask vTask =  iterator.next();
			args.add(filePathManager.getSharedFinalFile(vTask).getAbsolutePath());
			if (iterator.hasNext()) {
				args.add("+");
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
//				File partsDirectory = filePathManager.getSharedPartsFolder(task);
//				try {
//					// Clean job's parts
//					FileUtils.deleteDirectory(partsDirectory);
//					// Clean batch's parts folder if empty
//					File superPartsDirectory = partsDirectory.getParentFile();
//					if (superPartsDirectory.list().length == 0) {
//						superPartsDirectory.delete();
//					}
//				} catch (IOException e) {
//					Logger logger = Logger.getLogger("lancoder");
//					logger.warning(e.getMessage());
//				}
				this.listener.jobMuxingCompleted(task);
			} else {
				this.listener.jobMuxingFailed(task);
			}
		}
	}

}
