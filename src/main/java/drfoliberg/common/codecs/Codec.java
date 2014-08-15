package drfoliberg.common.codecs;

public enum Codec {

	UNKNOWN("Unknown","none", "unknown", "unknown"),
	// Audio codecs
	/**
	 * Vorbis codec
	 */
	VORBIS("Vorbis","vorbis", "libvorbis", "ogg"),
	/**
	 * Opus
	 */
	OPUS("Opus","libopus", "libopus", "ogg"),
	/**
	 * AAC
	 */
	AAC("Advanced Audio Coding","aac", "libfdk_aac", "m4a"),
	/**
	 * FLAC
	 */
	FLAC("FLAC","flac", "flac", "flac"),
	/**
	 * Mp3
	 */
	MP3("MP3","mp3", "libmp3lame", "mp3"),
	/**
	 * DTS or DCA (DTS Coherent Acoustics)
	 */
	DTS("DTS","dca", "dca", "dts"),
	/**
	 * Copy the original stream
	 */
	COPY("Copy audio stream","none", "copy", "mkv"),

	// Video codecs
	/**
	 * H.264
	 */
	H264("H.264/MPEG-4 AVC","h264", "libx264", "mpegts"),
	/**
	 * H.265 (This encoding is not yet supported by most systems.)
	 */
	H265("HEVC/H.265","h265", "libx265", "mpegts"),

//	MPEG4("MPEG-4", "mpeg4", "mp4"),

	// Subtitles
	SUBRIP("Subrip","subrip", "subrip", "srt");

	private String prettyName;
	private String ffMpegName;
	private String encoder;
	private String container;

	private Codec(String name,String ffMpegName, String encoder, String container) {
		this.prettyName = name;
		this.ffMpegName = ffMpegName;
		this.encoder = encoder;
		this.container = container;
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

	public String getFfMpegName() {
		return ffMpegName;
	}

}
