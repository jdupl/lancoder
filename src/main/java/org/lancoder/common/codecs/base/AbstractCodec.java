package org.lancoder.common.codecs.base;

import org.lancoder.common.codecs.CodecEnum;

public abstract class AbstractCodec {

	private String prettyName;
	private String ffMpegName;
	private String encoder;
	private String container;
	private boolean lossless;

	protected AbstractCodec(CodecEnum codecEnum) {
		this(codecEnum.getPrettyName(), codecEnum.getFFMpegName(), codecEnum.getEncoder(), codecEnum.getContainer(),
				codecEnum.isLossless());
	}

	private AbstractCodec(String name, String ffMpegName, String encoder, String container, boolean lossless) {
		this.prettyName = name;
		this.ffMpegName = ffMpegName;
		this.encoder = encoder;
		this.container = container;
		this.lossless = lossless;
	}

	public abstract String getTypeSwitch();

	/**
	 * Format a given bitrate in Kb/s
	 * 
	 * @param bitrate
	 *            in Kb/s
	 * @return The ffmpeg's notation for this codec.
	 */
	public String formatBitrate(int bitrate) {
		return String.format("%dk");
	}

	public boolean supportsPresets() {
		return false;
	}

	public String getCRFSwitchArg() {
		return String.format("-%s:%s", getCRFSwitch(), getTypeSwitch());
	}

	public String getVBRSwitchArg() {
		return String.format("-%s:%s", getVBRSwitch(), getTypeSwitch());
	}

	protected String getCRFSwitch() {
		return "q";
	}

	protected String getVBRSwitch() {
		return "b";
	}

	public String getPrettyName() {
		return prettyName;
	}

	public String getFfMpegName() {
		return ffMpegName;
	}

	public String getEncoder() {
		return encoder;
	}

	public String getContainer() {
		return container;
	}

	public boolean isLossless() {
		return lossless;
	}

	public boolean needsTranscode() {
		return false;
	}

}
