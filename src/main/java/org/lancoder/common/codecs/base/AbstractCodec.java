package org.lancoder.common.codecs.base;

import java.io.Serializable;

import org.lancoder.common.codecs.CodecEnum;

public abstract class AbstractCodec implements Serializable {

	private static final long serialVersionUID = -3632642598335400603L;
	private String prettyName;
	private String ffMpegName;
	private String encoder;
	private String container;
	private boolean lossless;
	private CodecEnum codecEnum;

	protected AbstractCodec(CodecEnum codecEnum) {
		this(codecEnum.getPrettyName(), codecEnum.getFFMpegName(), codecEnum.getEncoder(), codecEnum.getContainer(),
				codecEnum.isLossless());
		this.codecEnum = codecEnum;
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
		return String.format("%dk", bitrate);
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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractCodec) {
			AbstractCodec other = (AbstractCodec) obj;
			return other.ffMpegName.equals(this.ffMpegName);
		}
		return false;
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

	public CodecEnum getCodecEnum() {
		return codecEnum;
	}

	public String formatQuality(int rate) {
		return String.valueOf(rate);
	}

	/**
	 * Allows some codec to block or silently change some unsupported sampling rates.
	 *
	 * @param hz
	 *            The sample frequency in hz.
	 * @return The corresponding string representing the nearest frequency
	 */
	public String formatHz(int hz) {
		return String.valueOf(hz);
	}

	public String formatHz(String sampleRate) {
		return formatHz(Integer.parseInt(sampleRate));
	}

}
