package drfoliberg.common.task.audio;

public enum AudioCodec {

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
	 * Copy the original stream
	 */
	COPY("Copy audio stream", "copy", "mka");

	private String name;
	private String encoder;
	private String container;

	private AudioCodec(String name, String encoder, String container) {
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
