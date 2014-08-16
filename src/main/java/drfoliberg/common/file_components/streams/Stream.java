package drfoliberg.common.file_components.streams;

import java.io.Serializable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import drfoliberg.common.codecs.Codec;

public class Stream implements Serializable {

	private static final long serialVersionUID = -1867430611531693710L;
	protected int index;
	protected Codec codec = Codec.UNKNOWN;
	protected String title = "";
	protected String language = "und";
	protected boolean isDefault = false;

	/**
	 * Parse a json object and instantiate a stream with it's properties.
	 * 
	 * @param json
	 *            the json stream object to parse
	 */
	public Stream(JsonObject json) {
		this.index = json.get("index").getAsInt();
		String unknownCodec = json.get("codec_name").getAsString();
		for (Codec codec : Codec.values()) {
			if (codec.getFfMpegName().equalsIgnoreCase(unknownCodec)) {
				this.codec = codec;
				break;
			}
		}
		JsonElement tagsElement = json.get("tags");
		if (tagsElement != null) {
			JsonObject tags = tagsElement.getAsJsonObject();
			if (tags.get("title") != null) {
				this.title = tags.get("title").getAsString();
			}
			if (tags.get("language") != null) {
				this.language = tags.get("language").getAsString();
			}
		}

		JsonElement dispositionElement = json.get("disposition");
		if (dispositionElement != null) {
			JsonObject disposition = dispositionElement.getAsJsonObject();
			if (disposition.get("default") != null) {
				this.isDefault = disposition.get("default").getAsInt() == 0 ? false : true;
			}
		}
	}

	public int getIndex() {
		return index;
	}

	public Codec getCodec() {
		return codec;
	}

	public String getTitle() {
		return title;
	}

	public String getLanguage() {
		return language;
	}

	public boolean isDefault() {
		return isDefault;
	}

}
