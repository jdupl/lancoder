package org.lancoder.worker.converter.audio;

import org.lancoder.common.FilePathManager;
import org.lancoder.common.pool.Pooler;
import org.lancoder.common.task.audio.ClientAudioTask;
import org.lancoder.common.third_parties.FFmpeg;
import org.lancoder.worker.converter.ConverterListener;
import org.lancoder.worker.converter.ConverterPool;

public class AudioConverterPool extends ConverterPool<ClientAudioTask> {

	private ConverterListener listener;
	private FilePathManager filePathManager;
	private FFmpeg ffMpeg;

	public AudioConverterPool(int threads, ConverterListener listener, FilePathManager filePathManager, FFmpeg ffMpeg) {
		super(threads, false);
		this.listener = listener;
		this.filePathManager = filePathManager;
		this.ffMpeg = ffMpeg;
	}

	@Override
	protected Pooler<ClientAudioTask> getPoolerInstance() {
		return new AudioWorkThread(listener, filePathManager, ffMpeg);
	}

}
