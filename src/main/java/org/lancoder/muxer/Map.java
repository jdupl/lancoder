package org.lancoder.muxer;

public class Map {

	private int inputFileIndex;
	private int inputStreamIndex;

	public Map(int inputFileIndex, int streamIndex) {
		this.inputFileIndex = inputFileIndex;
		this.inputStreamIndex = streamIndex;
	}

	@Override
	public String toString() {
		return String.format("%d:%d", inputFileIndex, inputStreamIndex);
	}

}
