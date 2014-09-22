package org.lancoder.common.codecs;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class CodecAdapterFactory implements TypeAdapterFactory {

	@Override
	public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
		Class<? super T> rawType = type.getRawType();
		if (rawType == Codec.class) {
			return new MyEnumTypeAdapter<T>();
		}
		return null;
	}

	public class MyEnumTypeAdapter<T> extends TypeAdapter<T> {

		public void write(JsonWriter out, T value) throws IOException {
			if (value == null) {
				out.nullValue();
				return;
			}
			Codec codec = (Codec) value;
			out.beginObject();
			out.name("name");
			out.value(codec.name());
			out.name("value");
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

}