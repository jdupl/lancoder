package org.lancoder.common.codecs;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Type adapter for Gson serialization
 */
public class CodecTypeAdapter<T> extends TypeAdapter<T> {

	public void write(JsonWriter out, T value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		Codec codec = (Codec) value;
		out.beginObject();
		out.name("value");
		out.value(codec.name());
		out.name("name");
		out.value(codec.getPrettyName());
		out.name("lossless");
		out.value(codec.isLossless());
		out.endObject();
	}

	@Override
	public T read(JsonReader in) throws IOException {
		throw new UnsupportedOperationException();
	}
}