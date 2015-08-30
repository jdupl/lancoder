package org.lancoder.common;

import org.lancoder.common.annotations.NoWebUI;
import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.codecs.CodecTypeAdapter;
import org.lancoder.common.file_components.FileInfo;
import org.lancoder.common.network.messages.web.ApiJobRequest;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class FakeInfo {

	public static ApiJobRequest fakeAudioEncodeRequest() {
		String json = "{" +
				"    \"rateControlType\":\"VBR\"," +
				"    \"passes\":1," +
				"    \"rate\":1500," +
				"    \"preset\":\"MEDIUM\"," +
				"    \"audioConfig\":\"AUTO\"," +
				"    \"audioRateControlType\":\"CRF\"," +
				"    \"audioRate\":5," +
				"    \"audioChannels\":\"STEREO\"," +
				"    \"audioSampleRate\":48000," +
				"    \"audioCodec\":{" +
				"        \"value\":\"VORBIS\"," +
				"        \"name\":\"Vorbis\"," +
				"        \"lossless\":false," +
				"        \"library\":\"libvorbis\"" +
				"    }," +
				"    \"videoCodec\":{" +
				"        \"value\":\"H264\"," +
				"        \"name\":\"H.264/MPEG-4 AVC\"," +
				"        \"lossless\":false," +
				"        \"library\":\"libx264\"" +
				"    }," +
				"    \"name\":\"testJob\"," +
				"    \"inputFile\":\"testInput.mkv\"" +
				"}";
		Gson gson = new GsonBuilder().registerTypeAdapter(CodecEnum.class, new CodecTypeAdapter<>())
				.setExclusionStrategies(new ExclusionStrategy() {
					@Override
					public boolean shouldSkipField(FieldAttributes f) {
						return f.getAnnotation(NoWebUI.class) != null;
					}

					@Override
					public boolean shouldSkipClass(Class<?> clazz) {
						return false;
					}
				}).serializeSpecialFloatingPointValues().create();

		return gson.fromJson(json, ApiJobRequest.class);
	}

	public static ApiJobRequest fakeAudioCopyRequest() {
		String json = "{" +
				"    \"rateControlType\":\"VBR\"," +
				"    \"passes\":1," +
				"    \"rate\":1500," +
				"    \"preset\":\"MEDIUM\"," +
				"    \"audioConfig\":\"COPY\"," +
				"    \"audioRateControlType\":\"CRF\"," +
				"    \"audioRate\":5," +
				"    \"audioChannels\":\"STEREO\"," +
				"    \"audioSampleRate\":48000," +
				"    \"audioCodec\":{" +
				"        \"value\":\"VORBIS\"," +
				"        \"name\":\"Vorbis\"," +
				"        \"lossless\":false," +
				"        \"library\":\"libvorbis\"" +
				"    }," +
				"    \"videoCodec\":{" +
				"        \"value\":\"H264\"," +
				"        \"name\":\"H.264/MPEG-4 AVC\"," +
				"        \"lossless\":false," +
				"        \"library\":\"libx264\"" +
				"    }," +
				"    \"name\":\"testJob\"," +
				"    \"inputFile\":\"testInput.mkv\"" +
				"}";
		Gson gson = new GsonBuilder().registerTypeAdapter(CodecEnum.class, new CodecTypeAdapter<>())
				.setExclusionStrategies(new ExclusionStrategy() {
					@Override
					public boolean shouldSkipField(FieldAttributes f) {
						return f.getAnnotation(NoWebUI.class) != null;
					}

					@Override
					public boolean shouldSkipClass(Class<?> clazz) {
						return false;
					}
				}).serializeSpecialFloatingPointValues().create();

		return gson.fromJson(json, ApiJobRequest.class);
	}


	public static FileInfo fakeFileInfo() {
		JsonParser p = new JsonParser();
		JsonElement json = p.parse("{" +
				"    \"streams\": [" +
				"        {" +
				"            \"index\": 0," +
				"            \"codec_name\": \"h264\"," +
				"            \"codec_long_name\": \"H.264 / AVC / MPEG-4 AVC / MPEG-4 part 10\"," +
				"            \"profile\": \"Main\"," +
				"            \"codec_type\": \"video\"," +
				"            \"codec_time_base\": \"1/4800\"," +
				"            \"codec_tag_string\": \"avc1\"," +
				"            \"codec_tag\": \"0x31637661\"," +
				"            \"width\": 1920," +
				"            \"height\": 1080," +
				"            \"coded_width\": 1920," +
				"            \"coded_height\": 1088," +
				"            \"has_b_frames\": 0," +
				"            \"sample_aspect_ratio\": \"0:1\"," +
				"            \"display_aspect_ratio\": \"0:1\"," +
				"            \"pix_fmt\": \"yuv420p\"," +
				"            \"level\": 41," +
				"            \"color_range\": \"tv\"," +
				"            \"color_space\": \"bt709\"," +
				"            \"color_transfer\": \"bt709\"," +
				"            \"color_primaries\": \"bt709\"," +
				"            \"chroma_location\": \"topleft\"," +
				"            \"refs\": 2," +
				"            \"is_avc\": \"1\"," +
				"            \"nal_length_size\": \"4\"," +
				"            \"r_frame_rate\": \"24/1\"," +
				"            \"avg_frame_rate\": \"24/1\"," +
				"            \"time_base\": \"1/2400\"," +
				"            \"start_pts\": 0," +
				"            \"start_time\": \"0.000000\"," +
				"            \"duration_ts\": 1431500," +
				"            \"duration\": \"596.458333\"," +
				"            \"bit_rate\": \"9282573\"," +
				"            \"bits_per_raw_sample\": \"8\"," +
				"            \"nb_frames\": \"14315\"," +
				"            \"disposition\": {" +
				"                \"default\": 1," +
				"                \"dub\": 0," +
				"                \"original\": 0," +
				"                \"comment\": 0," +
				"                \"lyrics\": 0," +
				"                \"karaoke\": 0," +
				"                \"forced\": 0," +
				"                \"hearing_impaired\": 0," +
				"                \"visual_impaired\": 0," +
				"                \"clean_effects\": 0," +
				"                \"attached_pic\": 0" +
				"            }," +
				"            \"tags\": {" +
				"                \"creation_time\": \"2008-05-27 18:40:35\"," +
				"                \"language\": \"eng\"," +
				"                \"handler_name\": \"Apple Alias Data Handler\"," +
				"                \"encoder\": \"H.264\"" +
				"            }" +
				"        }," +
				"        {" +
				"            \"index\": 1," +
				"            \"codec_type\": \"data\"," +
				"            \"codec_time_base\": \"1/24\"," +
				"            \"codec_tag_string\": \"tmcd\"," +
				"            \"codec_tag\": \"0x64636d74\"," +
				"            \"r_frame_rate\": \"0/0\"," +
				"            \"avg_frame_rate\": \"0/0\"," +
				"            \"time_base\": \"1/2400\"," +
				"            \"start_pts\": 0," +
				"            \"start_time\": \"0.000000\"," +
				"            \"duration_ts\": 1431508," +
				"            \"duration\": \"596.461667\"," +
				"            \"nb_frames\": \"1\"," +
				"            \"disposition\": {" +
				"                \"default\": 1," +
				"                \"dub\": 0," +
				"                \"original\": 0," +
				"                \"comment\": 0," +
				"                \"lyrics\": 0," +
				"                \"karaoke\": 0," +
				"                \"forced\": 0," +
				"                \"hearing_impaired\": 0," +
				"                \"visual_impaired\": 0," +
				"                \"clean_effects\": 0," +
				"                \"attached_pic\": 0" +
				"            }," +
				"            \"tags\": {" +
				"                \"creation_time\": \"2008-05-27 18:40:35\"," +
				"                \"language\": \"eng\"," +
				"                \"handler_name\": \"Apple Alias Data Handler\"," +
				"                \"timecode\": \"00:00:00:00\"" +
				"            }" +
				"        }," +
				"        {" +
				"            \"index\": 2," +
				"            \"codec_name\": \"aac\"," +
				"            \"codec_long_name\": \"AAC (Advanced Audio Coding)\"," +
				"            \"profile\": \"LC\"," +
				"            \"codec_type\": \"audio\"," +
				"            \"codec_time_base\": \"1/48000\"," +
				"            \"codec_tag_string\": \"mp4a\"," +
				"            \"codec_tag\": \"0x6134706d\"," +
				"            \"sample_fmt\": \"fltp\"," +
				"            \"sample_rate\": \"48000\"," +
				"            \"channels\": 6," +
				"            \"channel_layout\": \"5.1\"," +
				"            \"bits_per_sample\": 0," +
				"            \"r_frame_rate\": \"0/0\"," +
				"            \"avg_frame_rate\": \"0/0\"," +
				"            \"time_base\": \"1/48000\"," +
				"            \"start_pts\": 0," +
				"            \"start_time\": \"0.000000\"," +
				"            \"duration_ts\": 28631040," +
				"            \"duration\": \"596.480000\"," +
				"            \"bit_rate\": \"437605\"," +
				"            \"nb_frames\": \"27960\"," +
				"            \"disposition\": {" +
				"                \"default\": 1," +
				"                \"dub\": 0," +
				"                \"original\": 0," +
				"                \"comment\": 0," +
				"                \"lyrics\": 0," +
				"                \"karaoke\": 0," +
				"                \"forced\": 0," +
				"                \"hearing_impaired\": 0," +
				"                \"visual_impaired\": 0," +
				"                \"clean_effects\": 0," +
				"                \"attached_pic\": 0" +
				"            }," +
				"            \"tags\": {" +
				"                \"creation_time\": \"2008-05-27 18:40:35\"," +
				"                \"language\": \"eng\"," +
				"                \"handler_name\": \"Apple Alias Data Handler\"" +
				"            }" +
				"        }" +
				"    ]," +
				"    \"format\": {" +
				"        \"filename\": \"Vidéos/multi/big_buck_bunny_1080p_h264.mov\"," +
				"        \"nb_streams\": 3," +
				"        \"nb_programs\": 0," +
				"        \"format_name\": \"mov,mp4,m4a,3gp,3g2,mj2\"," +
				"        \"format_long_name\": \"QuickTime / MOV\"," +
				"        \"start_time\": \"0.000000\"," +
				"        \"duration\": \"596.461667\"," +
				"        \"size\": \"725106140\"," +
				"        \"bit_rate\": \"9725434\"," +
				"        \"probe_score\": 100," +
				"        \"tags\": {" +
				"            \"major_brand\": \"qt  \"," +
				"            \"minor_version\": \"537199360\"," +
				"            \"compatible_brands\": \"qt  \"," +
				"            \"creation_time\": \"2008-05-27 18:40:35\"," +
				"            \"timecode\": \"00:00:00:00\"" +
				"        }" +
				"    }" +
				"}");
		return new FileInfo(json.getAsJsonObject(), "source");
	}

}
