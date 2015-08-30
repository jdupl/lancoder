package org.lancoder.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
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
import org.lancoder.muxer.MKvMergeMuxer;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FFmpegWrapper.class, Job.class })
public class MkvMergeMuxerTest {

	@Test
	public void testMuxConcatVideoTrackAndOneAudioTrack() {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder("/shared");
		config.setTempEncodingFolder("/tmp");

		FilePathManager filePathManager = new FilePathManager(config);
		JobInitiator jobInitiator = new JobInitiator(null, config);

		MkvMerge mkvMerge = new MkvMerge(config);

		MKvMergeMuxer muxer = new MKvMergeMuxer(null, filePathManager, mkvMerge);

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

		muxer.handle(job);

		ArrayList<String> expected = new ArrayList<>(Arrays.asList(new String[] { "mkvmerge", "-o",
				"/shared/encodes/testJob/testSource.mkv", "/shared/encodes/testJob/parts/2/part-2.ogg",
				"/shared/encodes/testJob/parts/0/part-0.mkv", "+", "/shared/encodes/testJob/parts/1/part-1.mkv" }));

		assertEquals(expected, muxer.getArgs());
	}

	@Test
	public void testMuxConcatVideoTrackAndOneAudioCopyTrack() {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder("/shared");
		config.setTempEncodingFolder("/tmp");

		FilePathManager filePathManager = new FilePathManager(config);
		JobInitiator jobInitiator = new JobInitiator(null, config);

		MkvMerge mkvMerge = new MkvMerge(config);

		MKvMergeMuxer muxer = new MKvMergeMuxer(null, filePathManager, mkvMerge);

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

		muxer.handle(job);

		ArrayList<String> expected = new ArrayList<>(Arrays.asList(new String[] { "mkvmerge", "-o",
				"/shared/encodes/testJob/testSource.mkv", "/shared/encodes/testJob/parts/0/part-0.mkv", "+",
				"/shared/encodes/testJob/parts/1/part-1.mkv", "-a", "2", "/shared/testSource.mkv" }));

		assertEquals(expected, muxer.getArgs());
	}

	@Test
	public void testMuxConcatVideoTrackAndManyAudioCopyTrack() {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder("/shared");
		config.setTempEncodingFolder("/tmp");

		FilePathManager filePathManager = new FilePathManager(config);
		JobInitiator jobInitiator = new JobInitiator(null, config);

		MkvMerge mkvMerge = new MkvMerge(config);

		MKvMergeMuxer muxer = new MKvMergeMuxer(null, filePathManager, mkvMerge);

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

		muxer.handle(job);

		ArrayList<String> expected = new ArrayList<>(Arrays.asList(new String[] { "mkvmerge", "-o",
				"/shared/encodes/testJob/testSource.mkv", "/shared/encodes/testJob/parts/0/part-0.mkv", "+",
				"/shared/encodes/testJob/parts/1/part-1.mkv", "-a", "3,2", "/shared/testSource.mkv" }));

		assertEquals(expected, muxer.getArgs());
	}
}
