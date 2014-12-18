package org.lancoder.common.file_components.streams;

import com.google.gson.JsonObject;

public class TextStream extends Stream {

	private static final long serialVersionUID = -5962727176951376293L;

	public TextStream(JsonObject json, String relativeSource) {
		super(json, relativeSource, 1);
	}

}
