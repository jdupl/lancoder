package org.lancoder.muxer;

import org.lancoder.common.FilePathManager;
import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.exceptions.MissingThirdPartyException;
import org.lancoder.common.job.Job;
import org.lancoder.common.pool.PoolWorker;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.common.third_parties.MkvMerge;

/**
 * Abstraction layer for the MuxerPool. Uses the correct implementation of the Muxer depending of the codec used for the job.
 *
 * @author justin
 *
 */
public class MuxerWorkerWrapper extends PoolWorker<Job> {

	private MkvMergeMuxer mkvMergeMuxer;
	private FFmpegMuxer ffmpegMuxer;
	private MuxerListener listener;

	public MuxerWorkerWrapper(FFmpeg ffmpeg, MkvMerge mkvMerge, FilePathManager filePathManager, MuxerListener listener) {
		this.mkvMergeMuxer = new MkvMergeMuxer(listener, filePathManager, mkvMerge);
		this.ffmpegMuxer = new FFmpegMuxer(listener, filePathManager, ffmpeg);
		this.listener = listener;
	}

	@Override
	protected void start() {
		// Use mkvmerge only if h265 is used
		if (task.getClientVideoTasks().get(0).getStreamConfig().getOutStream().getCodec().getCodecEnum() == CodecEnum.H265) {
			sendJobToMuxer(mkvMergeMuxer);
		} else {
			sendJobToMuxer(ffmpegMuxer);
		}
	}

	private void sendJobToMuxer(Muxer muxer) {
		if (muxer.getMuxingThirdParty().isInstalled()) {
			muxer.handle(task);
		} else {
			listener.jobMuxingFailed(task);
			throw new MissingThirdPartyException(muxer.getMuxingThirdParty());
		}
	}
}
