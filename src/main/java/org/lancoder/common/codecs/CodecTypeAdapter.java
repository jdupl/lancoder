package org.lancoder.common.codecs;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Type adapter for Gson serialization
 */
public class CodecTypeAdapter<T> extends TypeAdapter<CodecEnum> {

	public void write(JsonWriter out, CodecEnum value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		CodecEnum codec = value;
		out.beginObject();
		out.name("value");
		out.value(codec.name());
		out.name("name");
		out.value(codec.getPrettyName());
		out.name("lossless");
		out.value(codec.isLossless());
		out.name("library");
		out.value(codec.getEncoder());
		out.endObject();
	}

	@Override
	public CodecEnum read(JsonReader in) throws IOException {
		in.beginObject();
		while (in.hasNext()) {
			if (in.nextName().equals("library")) {
				CodecEnum c = CodecEnum.findByLib(in.nextString());
				in.endObject();
				return c;
			} else {
				in.skipValue();
			}
		}
		in.endObject();
		return null;
	}
}
