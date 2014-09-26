package org.lancoder.common.codecs;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Type adapter for Gson serialization
 */
public class CodecTypeAdapter<T> extends TypeAdapter<Codec> {

	public void write(JsonWriter out, Codec value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		Codec codec = value;
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
	public Codec read(JsonReader in) throws IOException {
		in.beginObject();
		while (in.hasNext()) {
			if (in.nextName().equals("library")) {
				Codec c = Codec.findByLib(in.nextString());
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
