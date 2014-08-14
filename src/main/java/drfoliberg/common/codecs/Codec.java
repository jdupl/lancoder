package drfoliberg.common.codecs;

public enum Codec {
	// Audio codecs
	/**
	 * Vorbis codec
	 */
	VORBIS("Vorbis", "libvorbis", "ogg"),
	/**
	 * Opus
	 */
	OPUS("Opus", "libopus", "ogg"),
	/**
	 * AAC
	 */
	AAC("Advanced Audio Coding", "libfdk_aac", "m4a"),
	/**
	 * FLAC
	 */
	FLAC("FLAC", "flac", "flac"),
	/**
	 * Mp3
	 */
	MP3("Mp3", "libmp3lame", "mp3"),
	/**
	 * DTS or DCA (DTS Coherent Acoustics)
	 */
	DTS("DTS", "dca", "dts"),
	/**
	 * Copy the original stream
	 */
	COPY("Copy audio stream", "copy", "mkv"),

	// Video codecs
	/**
	 * H.264
	 */
	H264("H.264/MPEG-4 AVC", "libx264", "mpegts"),
	/**
	 * H.265 (This encoding is not yet supported by most systems.)
	 */
	H265("HEVC/H.265", "libx265", "mpegts"),

	// Subtitles
	SUBRIP("Subrip", "subrip", "srt");

	private String name;
	private String encoder;
	private String container;

	private Codec(String name, String encoder, String container) {
		this.name = name;
		this.encoder = encoder;
		this.container = container;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEncoder() {
		return encoder;
	}

	public void setEncoder(String encoder) {
		this.encoder = encoder;
	}

	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

}
