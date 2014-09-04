package org.lancoder.ffmpeg;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import org.lancoder.common.Service;

/**
 * Class to create a process, read its output and provide information about it's closing state.
 * 
 *
 */
public class FFmpegReader extends Service {

	Process p;
	FFmpegReaderListener listener;

	/**
	 * Create a process and read the desired output. Interface's onMessage method is called on each line.
	 * 
	 * @param args
	 *            The arguments of the command line
	 * @param listener
	 *            The listener
	 * @param useStdErr
	 * @return
	 */
	public boolean read(ArrayList<String> args, FFmpegReaderListener listener, boolean useStdErr) {
		this.listener = listener;
		boolean success = false;
		ProcessBuilder pb = new ProcessBuilder(args);
		Scanner s = null;
		try {
			Process p = pb.start();
			InputStream stream = useStdErr ? p.getErrorStream() : p.getInputStream();
			s = new Scanner(stream);
			while (s.hasNext() && !close) {
				listener.onMessage(s.next());
			}
			success = p.waitFor() == 0 ? true : false;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (close) {
				p.destroy();
			}
			if (s != null) {
				s.close();
			}
		}
		return success;
	}
}
