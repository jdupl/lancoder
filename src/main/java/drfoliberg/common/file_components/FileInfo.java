package drfoliberg.common.file_components;

import java.util.ArrayList;

import drfoliberg.common.file_components.streams.Stream;

public class FileInfo {
	private int duration;
	private int size;
	private int bitrate;
	private String format;
	private ArrayList<Stream> streams = new ArrayList<>();
	
}
