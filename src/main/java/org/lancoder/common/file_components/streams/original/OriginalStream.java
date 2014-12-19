package org.lancoder.common.file_components.streams.original;

import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.codecs.CodecLoader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class OriginalStream extends BaseStream {

	private static final long serialVersionUID = -281195855619472216L;

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
	public OriginalStream(JsonObject json, String relativeFile, long unitCount) {
		this.relativeFile = relativeFile;
		this.index = json.get("index").getAsInt();
		this.unitCount = unitCount;
		String unknownCodec = json.get("codec_name").getAsString();
		CodecEnum codecEnum = CodecEnum.findByLib(unknownCodec);
		this.codec = CodecLoader.fromCodec(codecEnum);
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
}
