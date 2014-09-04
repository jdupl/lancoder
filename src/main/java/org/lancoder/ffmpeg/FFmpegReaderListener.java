package org.lancoder.ffmpeg;

import org.lancoder.common.exceptions.MissingDecoderException;

public interface FFmpegReaderListener {

	public void onMessage(String line) throws MissingDecoderException;

}
