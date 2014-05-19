package drfoliberg.common.network.messages.api;

public class ApiJobRequest {
	private String name;
	private String inputFile;
	private int bitrate;

	public String getInputFile() {
		return inputFile;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public int getBitrate() {
		return bitrate;
	}

	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

}
