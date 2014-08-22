package org.lancoder.common.file_components;

import java.io.Serializable;
import java.util.ArrayList;

import org.lancoder.common.file_components.streams.AudioStream;
import org.lancoder.common.file_components.streams.Stream;
import org.lancoder.common.file_components.streams.TextStream;
import org.lancoder.common.file_components.streams.VideoStream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FileInfo implements Serializable {

	private static final long serialVersionUID = 5643049239410419603L;
	/**
	 * Duration of the file in ms
	 */
	private long duration;
	/**
	 * Size of the file in bytes
	 */
	private long size;
	/**
	 * Overall bitrate of the file in kbps
	 */
	private int bitrate;
	/**
	 * Format of the file
	 */
	private String format;
	/**
	 * Streams of the file
	 */
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

	public VideoStream getMainVideoStream() {
		ArrayList<VideoStream> vStreams = new ArrayList<>();
		for (Stream stream : streams) {
			if (stream instanceof VideoStream) {
				vStreams.add((VideoStream) stream);
			}
		}
		if (vStreams.size() > 0) {
			return vStreams.get(0);
		} else {
			return null;
		}
	}
	
	public ArrayList<VideoStream> getVideoStreams() {
		ArrayList<VideoStream> vStreams = new ArrayList<>();
		for (Stream stream : streams) {
			if (stream instanceof VideoStream) {
				vStreams.add((VideoStream) stream);
			}
		}
		return vStreams;
	}

	public ArrayList<AudioStream> getAudioStreams() {
		ArrayList<AudioStream> aStreams = new ArrayList<>();
		for (Stream stream : streams) {
			if (stream instanceof AudioStream) {
				aStreams.add((AudioStream) stream);
			}
		}
		return aStreams;
	}

	public ArrayList<TextStream> getTextStreams() {
		ArrayList<TextStream> tStreams = new ArrayList<>();
		for (Stream stream : streams) {
			if (stream instanceof TextStream) {
				tStreams.add((TextStream) stream);
			}
		}
		return tStreams;
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
