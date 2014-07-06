package main.java.drfoliberg.master.dispatcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import main.java.drfoliberg.common.Node;
import main.java.drfoliberg.common.network.Routes;
import main.java.drfoliberg.common.network.messages.cluster.TaskRequestMessage;
import main.java.drfoliberg.common.task.video.VideoEncodingTask;

import org.apache.commons.io.Charsets;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;

public class HttpDispatcher implements Runnable, DispatcherListener {
	Node node;
	VideoEncodingTask task;
	ArrayList<DispatcherListener> listeners;

	public HttpDispatcher(Node node, VideoEncodingTask task, DispatcherListener mainListener) {
		this.node = node;
		this.task = task;
		this.listeners = new ArrayList<>();
		this.listeners.add(mainListener);
	}

	@Override
	public void run() {
		boolean success = false;
		try {
			Gson gson = new Gson();
			CloseableHttpClient client = HttpClients.createDefault();
			RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000)
					.setConnectionRequestTimeout(2000).build();

			URI url = new URI("http", null, node.getNodeAddress().getHostAddress(), node.getNodePort(),
					Routes.ADD_TASK, null, null);
			HttpPut put = new HttpPut(url);
			put.setConfig(defaultRequestConfig);

			TaskRequestMessage trm = new TaskRequestMessage(task);

			StringEntity entity = new StringEntity(gson.toJson(trm));
			entity.setContentEncoding(Charsets.UTF_8.toString());
			entity.setContentType(ContentType.APPLICATION_JSON.toString());
			put.setEntity(entity);

			CloseableHttpResponse response = client.execute(put);
			if (response.getStatusLine().getStatusCode() == 200) {
				success = true;
			} else {
				System.err.printf("Node responded with bad status: %d\n", response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			System.err.println("Node timeout");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			if (success) {
				taskAccepted(task, node);
			} else {
				taskRefused(task, node);
			}
		}
	}

	@Override
	public void taskRefused(VideoEncodingTask t, Node n) {
		for (DispatcherListener dispatcherListener : listeners) {
			dispatcherListener.taskRefused(t, n);
		}

	}

	@Override
	public void taskAccepted(VideoEncodingTask t, Node n) {
		for (DispatcherListener dispatcherListener : listeners) {
			dispatcherListener.taskAccepted(t, n);
		}
	}
}