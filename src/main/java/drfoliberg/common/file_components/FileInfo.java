package drfoliberg.common.file_components;

import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import drfoliberg.common.file_components.streams.AudioStream;
import drfoliberg.common.file_components.streams.Stream;
import drfoliberg.common.file_components.streams.TextStream;
import drfoliberg.common.file_components.streams.VideoStream;

public class FileInfo {

	private long duration;
	private long size;
	private int bitrate;
	private String format;
	private ArrayList<Stream> streams = new ArrayList<>();

	public FileInfo(JsonObject json) {
		JsonObject format = json.get("format").getAsJsonObject();
		this.setBitrate(format.get("bit_rate").getAsInt());
		this.setSize(format.get("size").getAsLong());
		// convert from seconds to ms
		this.setDuration((long) (format.get("duration").getAsDouble() * 1000));
		this.setFormat(format.get("format_name").getAsString());

		for (JsonElement jsonStream : json.get("streams").getAsJsonArray()) {
			JsonObject jsonObject = jsonStream.getAsJsonObject();
			switch (jsonObject.get("codec_type").getAsString()) {
			case "video":
				this.getStreams().add(new VideoStream(jsonObject));
				break;
			case "audio":
				this.getStreams().add(new AudioStream(jsonObject));
				break;
			case "subtitle":
				this.getStreams().add(new TextStream(jsonObject));
				break;
			default:
				break;
			}
		}
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public int getBitrate() {
		return bitrate;
	}

	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public ArrayList<Stream> getStreams() {
		return streams;
	}

}
