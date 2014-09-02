package org.lancoder;

import java.io.IOException;

public class MainSimulation {

	/**
	 * A launch method of the program to only run a local simulation.
	 * 
	 * @throws IOException
	 * @see Simulation
	 */
	public static void main(String[] args) throws IOException {
		Simulation s = new Simulation();
		s.run();
	}
}
