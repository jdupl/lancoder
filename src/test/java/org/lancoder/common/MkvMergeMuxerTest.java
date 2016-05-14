package org.lancoder.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lancoder.common.job.Job;
import org.lancoder.common.third_parties.FFprobe;
import org.lancoder.common.third_parties.MkvMerge;
import org.lancoder.ffmpeg.FFmpegWrapper;
import org.lancoder.master.JobInitiator;
import org.lancoder.master.MasterConfig;
import org.lancoder.muxer.MkvMergeMuxer;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FFmpegWrapper.class, Job.class })
public class MkvMergeMuxerTest {

	public String normalizePath(String path) {
		if (File.separatorChar == '\\' && path.startsWith("/"))
			/* Noob workaround, I am not able to use SystemUtils.IS_OS_WINDOWS because of being a noob. */
			path = "C:" + path;
		   return path.replace("/", File.separator);
		}
	
	@Test
	public void testMuxConcatVideoTrackAndOneAudioTrack() throws Exception {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder(normalizePath("/shared"));
		config.setTempEncodingFolder(normalizePath("/tmp"));

		FilePathManager filePathManager = new FilePathManager(config);
		JobInitiator jobInitiator = new JobInitiator(null, config);

		MkvMerge mkvMerge = new MkvMerge(config);

		MkvMergeMuxer muxer = new MkvMergeMuxer(null, filePathManager, mkvMerge);

		PowerMockito.mockStatic(FFmpegWrapper.class);
		Mockito.when(FFmpegWrapper.getFileInfo((File) Mockito.any(), (String) Mockito.any(), (FFprobe) Mockito.any()))
				.thenReturn(FakeInfo.fakeFileInfo());

		Job job = null;
		try {
			job = Whitebox.<Job> invokeMethod(jobInitiator, FakeInfo.fakeAudioEncodeRequest(), new File(
					"testSource.mkv"));
		} catch (Exception e) {
			fail();
		}

		Field field = muxer.getClass().getDeclaredField("job");
		field.setAccessible(true);
		field.set(muxer, job);

		ArrayList<String> expected = new ArrayList<>(Arrays.asList(new String[] { "mkvmerge", "-o",
				normalizePath("/shared/encodes/testJob/testSource.mkv"),
				normalizePath("/shared/encodes/testJob/parts/0/part-0.mkv"), "+", normalizePath("/shared/encodes/testJob/parts/1/part-1.mkv"), normalizePath("/shared/encodes/testJob/parts/2/part-2.ogg") }));


		assertEquals(expected, muxer.getArgs());
	}

	@Test
	public void testMuxConcatVideoTrackAndOneAudioCopyTrack() throws Exception {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder(normalizePath("/shared"));
		config.setTempEncodingFolder(normalizePath("/tmp"));

		FilePathManager filePathManager = new FilePathManager(config);
		JobInitiator jobInitiator = new JobInitiator(null, config);

		MkvMerge mkvMerge = new MkvMerge(config);

		MkvMergeMuxer muxer = new MkvMergeMuxer(null, filePathManager, mkvMerge);

		PowerMockito.mockStatic(FFmpegWrapper.class);
		Mockito.when(FFmpegWrapper.getFileInfo((File) Mockito.any(), (String) Mockito.any(), (FFprobe) Mockito.any()))
				.thenReturn(FakeInfo.fakeFileInfo());

		Job job = null;
		try {
			job = Whitebox
					.<Job> invokeMethod(jobInitiator, FakeInfo.fakeAudioCopyRequest(), new File("testSource.mkv"));
		} catch (Exception e) {
			fail();
		}

		Field field = muxer.getClass().getDeclaredField("job");
		field.setAccessible(true);
		field.set(muxer, job);

		ArrayList<String> expected = new ArrayList<>(Arrays.asList(new String[] { "mkvmerge", "-o",
				normalizePath("/shared/encodes/testJob/testSource.mkv"), normalizePath("/shared/encodes/testJob/parts/0/part-0.mkv"), "+",
						normalizePath("/shared/encodes/testJob/parts/1/part-1.mkv"), "-a", "2", normalizePath("/shared/testSource.mkv") }));

		assertEquals(expected, muxer.getArgs());
	}

	@Test
	public void testMuxConcatVideoTrackAndManyAudioCopyTrack() throws Exception {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder(normalizePath("/shared"));
		config.setTempEncodingFolder(normalizePath("/tmp"));

		FilePathManager filePathManager = new FilePathManager(config);
		JobInitiator jobInitiator = new JobInitiator(null, config);

		MkvMerge mkvMerge = new MkvMerge(config);

		MkvMergeMuxer muxer = new MkvMergeMuxer(null, filePathManager, mkvMerge);

		PowerMockito.mockStatic(FFmpegWrapper.class);
		Mockito.when(FFmpegWrapper.getFileInfo((File) Mockito.any(), (String) Mockito.any(), (FFprobe) Mockito.any()))
				.thenReturn(FakeInfo.fakeFileInfoMultiAudio());

		Job job = null;
		try {
			job = Whitebox
					.<Job> invokeMethod(jobInitiator, FakeInfo.fakeAudioCopyRequest(), new File("testSource.mkv"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		Field field = muxer.getClass().getDeclaredField("job");
		field.setAccessible(true);
		field.set(muxer, job);

		ArrayList<String> expected = new ArrayList<>(Arrays.asList(new String[] { "mkvmerge", "-o",
				normalizePath("/shared/encodes/testJob/testSource.mkv"), normalizePath("/shared/encodes/testJob/parts/0/part-0.mkv"), "+",
						normalizePath("/shared/encodes/testJob/parts/1/part-1.mkv"), "-a", "2,3", normalizePath("/shared/testSource.mkv") }));

		assertEquals(expected, muxer.getArgs());
	}
}
