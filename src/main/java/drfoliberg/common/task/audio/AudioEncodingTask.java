package drfoliberg.common.task.audio;

import drfoliberg.common.codecs.Codec;
import drfoliberg.common.job.RateControlType;
import drfoliberg.common.task.Task;

public class AudioEncodingTask extends Task {

	private static final long serialVersionUID = 1319651638856267785L;

	private Codec codec;
	private int channels;
	private int sampleRate;
	private int qualityRate;
	private String inputFile;
	private String outputFile;
	private RateControlType rateControlType;

	/**
	 * Creates an audio encoding task with handling of the extension and quality control
	 * 
	 * @param codec
	 * @param channels
	 * @param sampleRate
	 * @param qualityRate
	 * @param inputFile
	 * @param outputFile
	 * @return
	 */
	public AudioEncodingTask(Codec codec, int channels, int sampleRate, int qualityRate,
			RateControlType rateControlType, String inputFile, String outputFile, String jobId, int taskId) {
		super(jobId, taskId);
		this.codec = codec;
		this.channels = channels;
		this.sampleRate = sampleRate;
		this.qualityRate = qualityRate;
		this.rateControlType = rateControlType;
		this.inputFile = inputFile;
		this.outputFile = String.format("%s.%s", outputFile, codec.getContainer());
	}

	public Codec getCodec() {
		return codec;
	}

	public void setCodec(Codec codec) {
		this.codec = codec;
	}

	public int getChannels() {
		return channels;
	}

	public void setChannels(int channels) {
		this.channels = channels;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getQualityRate() {
		return qualityRate;
	}

	public void setQualityRate(int qualityRate) {
		this.qualityRate = qualityRate;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public RateControlType getRateControlType() {
		return rateControlType;
	}

	public void setRateControlType(RateControlType rateControlType) {
		this.rateControlType = rateControlType;
	}

}
