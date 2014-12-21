package org.lancoder.common.codecs;

import java.io.Serializable;

public enum CodecEnum implements Serializable {
	/**
	 * Unknown (unsupported) codec
	 */
	UNKNOWN("Unknown", "none", "unknown", "unknown", false),

	// Audio codecs
	/**
	 * Vorbis codec
	 */
	VORBIS("Vorbis", "vorbis", "libvorbis", "ogg", false),
	/**
	 * Opus
	 */
	OPUS("Opus", "libopus", "libopus", "ogg", false),
	/**
	 * AAC
	 */
	AAC("Advanced Audio Coding", "aac", "libfdk_aac", "m4a", false),
	/**
	 * FLAC
	 */
	FLAC("FLAC", "flac", "flac", "flac", true),
	/**
	 * Mp3
	 */
	MP3("MP3", "mp3", "libmp3lame", "mp3", false),
	/**
	 * DTS or DCA (DTS Coherent Acoustics)
	 */
	DTS("DTS", "dca", "dca", "dts", true),
	/**
	 * Speex
	 */
	SPEEX("Speex", "speex", "libspeex", "spx", false),
	/**
	 * Monkey's Audio
	 */
	APE("Monkey's Audio", "ape", "ape", "ape", true),
	/**
	 * WavPack
	 */
	WAVPACK("WavePack", "wavpack", "wavpack", "wv", true),

	// Video codecs
	/**
	 * H.264
	 */
	H264("H.264/MPEG-4 AVC", "h264", "libx264", "mkv", false),
	/**
	 * H.265 (This encoding is not yet supported by most systems.)
	 */
	H265("HEVC/H.265", "h265", "libx265", "mkv", false),

	THEORA("Theora", "theora", "libtheora", "ogg", false),
	/**
	 * Experimental codec by ffmpeg.
	 */
	VP8("WebM Vp8", "vp8", "libvpx", "webm", false),
	/**
	 * Experimental codec by ffmpeg.
	 */
	VP9("WebM Vp9", "vp9", "libvpx-vp9", "webm", false);

	private String prettyName;
	private String ffMpegName;
	private String encoder;
	private String container;
	private boolean lossless;

	private CodecEnum(String name, String ffMpegName, String encoder, String container, boolean lossless) {
		this.prettyName = name;
		this.ffMpegName = ffMpegName;
		this.encoder = encoder;
		this.container = container;
		this.lossless = lossless;
	}

	public boolean isLossless() {
		return lossless;
	}

	public String getPrettyName() {
		return prettyName;
	}

	public String getEncoder() {
		return encoder;
	}

	public String getContainer() {
		return container;
	}

	public String getFFMpegName() {
		return ffMpegName;
	}

	public static CodecEnum findByLib(String libname) {
		for (CodecEnum codec : CodecEnum.values()) {
			if (codec.getEncoder().equals(libname) || codec.getFFMpegName().equalsIgnoreCase(libname)) {
				return codec;
			}
		}
		return UNKNOWN;
	}

	public static CodecEnum[] getAudioCodecs() {
		return new CodecEnum[] { AAC, APE, DTS, FLAC, MP3, OPUS, SPEEX, VORBIS, WAVPACK };
	}

	public static CodecEnum[] getVideoCodecs() {
		return new CodecEnum[] { H264, H265, THEORA, VP9, VP8 };
	}

}
