package org.lancoder.common.file_components.streams;

import java.io.Serializable;
import java.util.ArrayList;

import org.lancoder.common.codecs.CodecEnum;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class Stream implements Serializable {

	private static final long serialVersionUID = -1867430611531693710L;

	protected String relativeFile;
	protected int index;
	protected CodecEnum codec = CodecEnum.UNKNOWN;

	protected String title = "";
	protected String language = "und";
	protected boolean isDefault = false;
	protected long unitCount;

	public Stream(int index, CodecEnum codec, long units, String relativeFile) {
		this.index = index;
		this.codec = codec;
		this.unitCount = units;
		this.relativeFile = relativeFile;
	}

	/**
	 * Parse a json object and instantiate a stream with it's properties.
	 * 
	 * @param json
	 *            the json stream object to parse
	 * @param relativeFile
	 *            The relative source file of this stream
	 * @param unitCount
	 * 
	 */
	public Stream(JsonObject json, String relativeFile, long unitCount) {
		this.relativeFile = relativeFile;
		this.index = json.get("index").getAsInt();
		this.unitCount = unitCount;
		String unknownCodec = json.get("codec_name").getAsString();
		this.codec = CodecEnum.findByLib(unknownCodec);
//		for (CodecEnum codec : CodecEnum.values()) {
//			if (codec.getFFMpegName().equalsIgnoreCase(unknownCodec)) {
//				this.codec = codec;
//				break;
//			}
//		}
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

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj != null && obj instanceof Stream) {
			Stream other = (Stream) obj;
			return other.index == this.index && other.codec.equals(this.codec);
		}
		return false;
	}

	@Deprecated
	public abstract ArrayList<String> getStreamCopyMapping();

	public long getUnitCount() {
		return unitCount;
	}

	public String getRelativeFile() {
		return relativeFile;
	}

	public int getIndex() {
		return index;
	}

	public CodecEnum getCodec() {
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
