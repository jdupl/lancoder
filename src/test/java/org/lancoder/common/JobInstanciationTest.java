package org.lancoder.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lancoder.common.job.Job;
import org.lancoder.common.task.video.ClientVideoTask;
import org.lancoder.common.third_parties.FFprobe;
import org.lancoder.ffmpeg.FFmpegWrapper;
import org.lancoder.master.JobInitiator;
import org.lancoder.master.MasterConfig;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FFmpegWrapper.class, Job.class})
public class JobInstanciationTest {

	public String normalizePath(String path) {
		   return path.replace("/", File.separator);
		}
	
	@Test
	public void testJobRelativePaths() {
		MasterConfig config = new MasterConfig();
		JobInitiator factory = new JobInitiator(null, config);

		PowerMockito.mockStatic(FFmpegWrapper.class);
		Mockito.when(FFmpegWrapper.getFileInfo((File) Mockito.any(), (String) Mockito.any(), (FFprobe) Mockito.any()))
				.thenReturn(FakeInfo.fakeFileInfo());

		Job job = null;
		try {
			job = Whitebox.<Job>invokeMethod(factory, "createJob", FakeInfo.fakeAudioEncodeRequest(), new File("output.mkv"));
		} catch (Exception e) {
			fail();
		}

		assertNotEquals("", job.getJobId());
		assertEquals("output.mkv", job.getOutputFileName());
		assertEquals(normalizePath("encodes/testJob"), job.getOutputFolder());
		assertEquals(normalizePath("encodes/testJob/parts"), job.getPartsFolderName());
	}

	@Test
	public void testTaskAreCreated() {
		MasterConfig config = new MasterConfig();
		JobInitiator factory = new JobInitiator(null, config);

		PowerMockito.mockStatic(FFmpegWrapper.class);
		Mockito.when(FFmpegWrapper.getFileInfo((File) Mockito.any(), (String) Mockito.any(), (FFprobe) Mockito.any()))
				.thenReturn(FakeInfo.fakeFileInfo());

		Job j = null;
		try {
			j = Whitebox.<Job>invokeMethod(factory, FakeInfo.fakeAudioEncodeRequest(), new File(""));
		} catch (Exception e) {
			fail();
		}
		assertEquals(1, j.getClientAudioTasks().size());
		assertEquals(2, j.getClientVideoTasks().size());

		ClientVideoTask task1 = j.getClientVideoTasks().get(0);
		assertEquals(0, task1.getEncodingStartTime());
		assertEquals(300000, task1.getEncodingEndTime());

		ClientVideoTask task2 = j.getClientVideoTasks().get(1);
		assertEquals(300000, task2.getEncodingStartTime());
		assertEquals(596461, task2.getEncodingEndTime());
	}

	@Test
	public void testTaskRelativePaths() {
		MasterConfig config = new MasterConfig();
		JobInitiator factory = new JobInitiator(null, config);

		PowerMockito.mockStatic(FFmpegWrapper.class);
		Mockito.when(FFmpegWrapper.getFileInfo((File) Mockito.any(), (String) Mockito.any(), (FFprobe) Mockito.any()))
				.thenReturn(FakeInfo.fakeFileInfo());

		PowerMockito.spy(Job.class);
		try {
			PowerMockito.doReturn("testJobId").when(Job.class, "generateId", Mockito.any(), Mockito.any());
		} catch (Exception e) {
			fail(e.getMessage());
		}

		Job j = null;
		try {
			j = Whitebox.<Job>invokeMethod(factory, FakeInfo.fakeAudioEncodeRequest(), new File(""));
		} catch (Exception e) {
			fail();
		}

		assertEquals(normalizePath("encodes/testJob/parts/0/part-0.mkv"), j.getClientTasks().get(0).getFinalFile().getPath());
		assertEquals(normalizePath("encodes/testJob/parts/1/part-1.mkv"), j.getClientTasks().get(1).getFinalFile().getPath());
		assertEquals(normalizePath("encodes/testJob/parts/2/part-2.ogg"), j.getClientTasks().get(2).getFinalFile().getPath());


		assertEquals(normalizePath("testJobId/0/part-0.mkv"), j.getClientTasks().get(0).getTempFile().getPath());
		assertEquals(normalizePath("testJobId/1/part-1.mkv"), j.getClientTasks().get(1).getTempFile().getPath());
		assertEquals(normalizePath("testJobId/2/part-2.ogg"), j.getClientTasks().get(2).getTempFile().getPath());
	}

	@Test
	public void testAbsolutePaths() {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder(normalizePath("/shared"));
		config.setTempEncodingFolder(normalizePath("/tmp"));

		FilePathManager manager = new FilePathManager(config);

		JobInitiator factory = new JobInitiator(null, config);

		PowerMockito.mockStatic(FFmpegWrapper.class);
		Mockito.when(FFmpegWrapper.getFileInfo((File) Mockito.any(), (String) Mockito.any(), (FFprobe) Mockito.any()))
				.thenReturn(FakeInfo.fakeFileInfo());

		PowerMockito.spy(Job.class);
		try {
			PowerMockito.doReturn("testJobId").when(Job.class, "generateId", Mockito.any(), Mockito.any());
		} catch (Exception e) {
			fail(e.getMessage());
		}

		Job j = null;
		try {
			j = Whitebox.<Job>invokeMethod(factory, FakeInfo.fakeAudioEncodeRequest(), new File("output.mkv"));
		} catch (Exception e) {
			fail();
		}

		assertEquals(normalizePath("/shared/encodes/testJob/parts/0/part-0.mkv"), manager.getSharedFinalFile(j.getClientTasks().get(0)).getPath());
		assertEquals(normalizePath("/tmp/testJobId/0/part-0.mkv"), manager.getLocalTempFile(j.getClientTasks().get(0)).getPath());
	}

}
