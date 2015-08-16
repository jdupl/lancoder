package org.lancoder.common.file_components;

import java.io.Serializable;
import java.util.ArrayList;

import org.lancoder.common.file_components.streams.original.OriginalAudioStream;
import org.lancoder.common.file_components.streams.original.OriginalStream;
import org.lancoder.common.file_components.streams.original.OriginalVideoStream;
import org.lancoder.common.file_components.streams.original.TextStream;

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
	private final ArrayList<OriginalStream> streams = new ArrayList<>();
	/**
	 * Relative path of this file
	 */
	private String relativeSource;

	/**
	 * Construct file info from streams in json object.
	 *
	 * @param json
	 *            The json object from ffprobe
	 * @param relativeSource
	 *            The relative path of the source file
	 */
	public FileInfo(JsonObject json, String relativeSource) {
		this.relativeSource = relativeSource;
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
				JsonObject disposition = jsonObject.getAsJsonObject("disposition");
				if (disposition == null  || ! isAttachment(disposition)) {
					this.getStreams().add(new OriginalVideoStream(jsonObject, relativeSource, duration));
				}

				break;
			case "audio":
				this.getStreams().add(new OriginalAudioStream(jsonObject, relativeSource, duration));
				break;
			case "subtitle":
				this.getStreams().add(new TextStream(jsonObject, relativeSource));
				break;
			default:
				break;
			}
		}
	}

	private boolean isAttachment(JsonObject disposition) {
		return disposition.get("attached_pic").getAsInt() == 0 ? false : true;
	}

	public OriginalVideoStream getMainVideoStream() {
		ArrayList<OriginalVideoStream> vStreams = getVideoStreams();
		if (vStreams.size() > 0) {
			return vStreams.get(0);
		} else {
			return null;
		}
	}

	public ArrayList<OriginalVideoStream> getVideoStreams() {
		ArrayList<OriginalVideoStream> vStreams = new ArrayList<>();
		for (OriginalStream stream : streams) {
			if (stream instanceof OriginalVideoStream) {
				vStreams.add((OriginalVideoStream) stream);
			}
		}
		return vStreams;
	}

	public ArrayList<OriginalAudioStream> getAudioStreams() {
		ArrayList<OriginalAudioStream> aStreams = new ArrayList<>();
		for (OriginalStream stream : streams) {
			if (stream instanceof OriginalAudioStream) {
				aStreams.add((OriginalAudioStream) stream);
			}
		}
		return aStreams;
	}

	public ArrayList<TextStream> getTextStreams() {
		ArrayList<TextStream> tStreams = new ArrayList<>();
		for (OriginalStream stream : streams) {
			if (stream instanceof TextStream) {
				tStreams.add((TextStream) stream);
			}
		}
		return tStreams;
	}

	public String getRelativeSource() {
		return relativeSource;
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

	public ArrayList<OriginalStream> getStreams() {
		return streams;
	}

}
