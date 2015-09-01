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

	@Test
	public void testMuxConcatVideoTrackAndOneAudioTrack() throws Exception {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder("/shared");
		config.setTempEncodingFolder("/tmp");

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
				"/shared/encodes/testJob/testSource.mkv",
				"/shared/encodes/testJob/parts/0/part-0.mkv", "+", "/shared/encodes/testJob/parts/1/part-1.mkv", "/shared/encodes/testJob/parts/2/part-2.ogg" }));


		assertEquals(expected, muxer.getArgs());
	}

	@Test
	public void testMuxConcatVideoTrackAndOneAudioCopyTrack() throws Exception {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder("/shared");
		config.setTempEncodingFolder("/tmp");

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
				"/shared/encodes/testJob/testSource.mkv", "/shared/encodes/testJob/parts/0/part-0.mkv", "+",
				"/shared/encodes/testJob/parts/1/part-1.mkv", "-a", "2", "/shared/testSource.mkv" }));

		assertEquals(expected, muxer.getArgs());
	}

	@Test
	public void testMuxConcatVideoTrackAndManyAudioCopyTrack() throws Exception {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder("/shared");
		config.setTempEncodingFolder("/tmp");

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
				"/shared/encodes/testJob/testSource.mkv", "/shared/encodes/testJob/parts/0/part-0.mkv", "+",
				"/shared/encodes/testJob/parts/1/part-1.mkv", "-a", "2,3", "/shared/testSource.mkv" }));

		assertEquals(expected, muxer.getArgs());
	}
}
