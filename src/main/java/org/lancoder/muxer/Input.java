package org.lancoder.muxer;

public class Input {

	private String inputFile;
	private int index;

	public Input(String inputFile, int index) {
		this.inputFile = inputFile;
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getInputFile() {
		return inputFile;
	}

}
