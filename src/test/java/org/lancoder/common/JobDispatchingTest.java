package org.lancoder.common;

import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lancoder.common.codecs.CodecEnum;
import org.lancoder.common.job.Job;
import org.lancoder.common.network.cluster.messages.TaskRequestMessage;
import org.lancoder.common.status.NodeState;
import org.lancoder.common.third_parties.FFprobe;
import org.lancoder.ffmpeg.FFmpegWrapper;
import org.lancoder.master.JobInitiator;
import org.lancoder.master.JobManager;
import org.lancoder.master.MasterConfig;
import org.lancoder.master.NodeManager;
import org.lancoder.master.dispatcher.DispatchItem;
import org.lancoder.master.dispatcher.DispatcherPool;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FFmpegWrapper.class, Job.class, NodeManager.class })
public class JobDispatchingTest {

	@Test
	public void testAudioTaskIsDispatchedBeforeVideoTasks() throws Exception {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder("/shared");
		config.setTempEncodingFolder("/tmp");

		JobInitiator jobInitiator = new JobInitiator(null, config);

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

		DispatcherPool dispatcherPool = PowerMockito.mock(DispatcherPool.class);

		NodeManager nodeManager = new NodeManager(null, config, null);
		JobManager jobManager = new JobManager(null, nodeManager, dispatcherPool, null, jobInitiator);

		HashMap<String, Job> jobs = new HashMap<>();
		jobs.put(job.getJobId(), job);

		Field field = jobManager.getClass().getDeclaredField("jobs");
		field.setAccessible(true);
		field.set(jobManager, jobs);

		ArrayList<CodecEnum> codecs = new ArrayList<>();
		codecs.add(CodecEnum.VORBIS);

		Node node = new Node(Inet4Address.getByAddress(new byte []{127,0,0,1}), 0, "node1", codecs, 4, "unid1");
		node.setStatus(NodeState.FREE);
		try {
			Whitebox.invokeMethod(nodeManager, "addNode", node);
		} catch (Exception e) {
			e.printStackTrace();
			// catches null pointer (listener)
		}

		jobManager.updateNodesWork();
		Mockito.verify(dispatcherPool).add(new DispatchItem(new TaskRequestMessage(job.getClientAudioTasks().get(0)), node));
	}

	@Test
	public void testNothingIsDispatchedIfNodeIsBusy() throws Exception {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder("/shared");
		config.setTempEncodingFolder("/tmp");

		JobInitiator jobInitiator = new JobInitiator(null, config);

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

		DispatcherPool dispatcherPool = PowerMockito.mock(DispatcherPool.class);

		NodeManager nodeManager = new NodeManager(null, config, null);
		JobManager jobManager = new JobManager(null, nodeManager, dispatcherPool, null, jobInitiator);

		HashMap<String, Job> jobs = new HashMap<>();
		jobs.put(job.getJobId(), job);

		Field field = jobManager.getClass().getDeclaredField("jobs");
		field.setAccessible(true);
		field.set(jobManager, jobs);

		ArrayList<CodecEnum> codecs = new ArrayList<>();
		codecs.add(CodecEnum.VORBIS);

		Node node = new Node(Inet4Address.getByAddress(new byte []{127,0,0,1}), 0, "node1", codecs, 4, "unid1");
		node.setStatus(NodeState.FREE);
		try {
			Whitebox.invokeMethod(nodeManager, "addNode", node);
		} catch (Exception e) {
			e.printStackTrace();
			// catches null pointer (listener)
		}

		jobManager.updateNodesWork();
		Mockito.verify(dispatcherPool).add(new DispatchItem(new TaskRequestMessage(job.getClientAudioTasks().get(0)), node));
		jobManager.updateNodesWork();
		Mockito.verify(dispatcherPool, new Times(0));
	}


	@Test
	public void testMultipleAudio() throws Exception {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder("/shared");
		config.setTempEncodingFolder("/tmp");

		JobInitiator jobInitiator = new JobInitiator(null, config);

		PowerMockito.mockStatic(FFmpegWrapper.class);
		Mockito.when(FFmpegWrapper.getFileInfo((File) Mockito.any(), (String) Mockito.any(), (FFprobe) Mockito.any()))
				.thenReturn(FakeInfo.fakeFileInfoMultiAudio());

		Job job = null;
		try {
			job = Whitebox.<Job> invokeMethod(jobInitiator, FakeInfo.fakeAudioEncodeRequest(), new File(
					"testSource.mkv"));
		} catch (Exception e) {
			fail();
		}

		DispatcherPool dispatcherPool = PowerMockito.mock(DispatcherPool.class);

		NodeManager nodeManager = new NodeManager(null, config, null);
		JobManager jobManager = new JobManager(null, nodeManager, dispatcherPool, null, jobInitiator);

		HashMap<String, Job> jobs = new HashMap<>();
		jobs.put(job.getJobId(), job);

		Field field = jobManager.getClass().getDeclaredField("jobs");
		field.setAccessible(true);
		field.set(jobManager, jobs);

		ArrayList<CodecEnum> codecs = new ArrayList<>();
		codecs.add(CodecEnum.VORBIS);

		Node node = new Node(Inet4Address.getByAddress(new byte []{127,0,0,1}), 0, "node1", codecs, 4, "unid1");
		node.setStatus(NodeState.FREE);
		try {
			Whitebox.invokeMethod(nodeManager, "addNode", node);
		} catch (Exception e) {
			e.printStackTrace();
			// catches null pointer (listener)
		}

		jobManager.updateNodesWork();
		Mockito.verify(dispatcherPool).add(new DispatchItem(new TaskRequestMessage(job.getClientAudioTasks().get(0)), node));

		jobManager.updateNodesWork();
		Mockito.verify(dispatcherPool).add(new DispatchItem(new TaskRequestMessage(job.getClientAudioTasks().get(1)), node));
	}

	@Test
	public void testMultipleAudioAreNotDispatchedIfNodeFull() throws Exception {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder("/shared");
		config.setTempEncodingFolder("/tmp");

		JobInitiator jobInitiator = new JobInitiator(null, config);

		PowerMockito.mockStatic(FFmpegWrapper.class);
		Mockito.when(FFmpegWrapper.getFileInfo((File) Mockito.any(), (String) Mockito.any(), (FFprobe) Mockito.any()))
				.thenReturn(FakeInfo.fakeFileInfoMultiAudio());

		Job job = null;
		try {
			job = Whitebox.<Job> invokeMethod(jobInitiator, FakeInfo.fakeAudioEncodeRequest(), new File(
					"testSource.mkv"));
		} catch (Exception e) {
			fail();
		}

		DispatcherPool dispatcherPool = PowerMockito.mock(DispatcherPool.class);

		NodeManager nodeManager = new NodeManager(null, config, null);
		JobManager jobManager = new JobManager(null, nodeManager, dispatcherPool, null, jobInitiator);

		HashMap<String, Job> jobs = new HashMap<>();
		jobs.put(job.getJobId(), job);

		Field field = jobManager.getClass().getDeclaredField("jobs");
		field.setAccessible(true);
		field.set(jobManager, jobs);

		ArrayList<CodecEnum> codecs = new ArrayList<>();
		codecs.add(CodecEnum.VORBIS);

		Node node = new Node(Inet4Address.getByAddress(new byte []{127,0,0,1}), 0, "node1", codecs, 1, "unid1");
		node.setStatus(NodeState.FREE);
		try {
			Whitebox.invokeMethod(nodeManager, "addNode", node);
		} catch (Exception e) {
			e.printStackTrace();
			// catches null pointer (listener)
		}

		jobManager.updateNodesWork();
		Mockito.verify(dispatcherPool).add(new DispatchItem(new TaskRequestMessage(job.getClientAudioTasks().get(0)), node));

		jobManager.updateNodesWork();
		Mockito.verify(dispatcherPool, new Times(0));
	}

	@Test
	public void testMultipleNodeWithMultipleAudioTracks() throws Exception {
		MasterConfig config = new MasterConfig();
		config.setAbsoluteSharedFolder("/shared");
		config.setTempEncodingFolder("/tmp");

		JobInitiator jobInitiator = new JobInitiator(null, config);

		PowerMockito.mockStatic(FFmpegWrapper.class);
		Mockito.when(FFmpegWrapper.getFileInfo((File) Mockito.any(), (String) Mockito.any(), (FFprobe) Mockito.any()))
				.thenReturn(FakeInfo.fakeFileInfoMultiAudio());

		Job job = null;
		try {
			job = Whitebox.<Job> invokeMethod(jobInitiator, FakeInfo.fakeAudioEncodeRequest(), new File(
					"testSource.mkv"));
		} catch (Exception e) {
			fail();
		}

		DispatcherPool dispatcherPool = PowerMockito.mock(DispatcherPool.class);

		NodeManager nodeManager = new NodeManager(null, config, null);
		JobManager jobManager = new JobManager(null, nodeManager, dispatcherPool, null, jobInitiator);

		HashMap<String, Job> jobs = new HashMap<>();
		jobs.put(job.getJobId(), job);

		Field field = jobManager.getClass().getDeclaredField("jobs");
		field.setAccessible(true);
		field.set(jobManager, jobs);

		ArrayList<CodecEnum> codecs = new ArrayList<>();
		codecs.add(CodecEnum.VORBIS);
		codecs.add(CodecEnum.H264);

		Node node1 = new Node(Inet4Address.getByAddress(new byte []{127,0,0,1}), 0, "node1", codecs, 4, "unid1");
		node1.setStatus(NodeState.FREE);
		try {
			Whitebox.invokeMethod(nodeManager, "addNode", node1);
		} catch (Exception e) {
			e.printStackTrace();
			// catches null pointer (listener)
		}

		Node node2 = new Node(Inet4Address.getByAddress(new byte []{127,0,0,1}), 0, "node2", codecs, 4, "unid2");
		node2.setStatus(NodeState.FREE);
		try {
			Whitebox.invokeMethod(nodeManager, "addNode", node2);
		} catch (Exception e) {
			e.printStackTrace();
			// catches null pointer (listener)
		}

		jobManager.updateNodesWork();
		Mockito.verify(dispatcherPool).add(new DispatchItem(new TaskRequestMessage(job.getClientAudioTasks().get(0)), node1));
		Mockito.verify(dispatcherPool).add(new DispatchItem(new TaskRequestMessage(job.getClientVideoTasks().get(0)), node2));

		jobManager.updateNodesWork();
		Mockito.verify(dispatcherPool).add(new DispatchItem(new TaskRequestMessage(job.getClientAudioTasks().get(1)), node1));
	}


}
