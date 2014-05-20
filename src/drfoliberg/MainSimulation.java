package drfoliberg;

import java.io.IOException;

public class MainSimulation {

	final static String[] DEFAULT_FILE_PATHS = { "input.mkv" };

	/**
	 * The launch method of the program, which will determine whether to start a master node or worker node.<br/>
	 * FIXME : Not actually working at this state, only runs a local simulation.
	 * 
	 * @param filepaths
	 *            Paths of the video file(s) to encode.
	 * @throws IOException
	 * @see Simulation
	 */
	public static void main(String[] filepaths) throws IOException {
		if (filepaths.length == 0) {
			filepaths = DEFAULT_FILE_PATHS;
		}

		Simulation s = new Simulation();
		// The simulation ignores the rest of the files passed for testing reasons
		s.run(filepaths[0]);
	}

}
