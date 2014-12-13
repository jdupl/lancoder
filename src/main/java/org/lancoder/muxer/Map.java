package org.lancoder.muxer;

public class Map {

	private Input input;
	private int inputStreamIndex;

	public Map(Input input, int streamIndex) {
		this.input = input;
		this.inputStreamIndex = streamIndex;
	}

	@Override
	public String toString() {
		return String.format("%d:%d", input.getIndex(), inputStreamIndex);
	}

}
