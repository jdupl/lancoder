package org.lancoder.common.file_components.streams;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gson.JsonObject;

public class TextStream extends Stream {

	private static final long serialVersionUID = -5962727176951376293L;

	public TextStream(JsonObject json) {
		super(json);
	}

	@Override
	public ArrayList<String> getStreamCopyMapping() {
		ArrayList<String> args = new ArrayList<>();
		Collections.addAll(args, "-D", "-A", "-B", "--no-chapters", "-M", "--no-global-tags");
		return args;
	}

}
