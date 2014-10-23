package org.lancoder.worker.converter.video;

import java.util.ArrayList;

import org.lancoder.common.exceptions.MissingDecoderException;
import org.lancoder.common.exceptions.MissingThirdPartyException;
import org.lancoder.ffmpeg.FFmpegReader;
import org.lancoder.ffmpeg.FFmpegReaderListener;

/**
 * Dumb implementation of a FFmpegReader to ignore ffmpeg messages.
 * 
 * @author justin
 *
 */
public class Transcoder extends FFmpegReader implements FFmpegReaderListener {

	public boolean read(ArrayList<String> args) throws MissingDecoderException, MissingThirdPartyException {
		return super.read(args, this, true);
	}

	@Override
	public void onMessage(String line) {

	}

}
