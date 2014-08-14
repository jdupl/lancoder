package drfoliberg.master.dispatcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;

import org.apache.commons.io.Charsets;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.util.BlockingArrayQueue;

import com.google.gson.Gson;

import drfoliberg.common.Node;
import drfoliberg.common.RunnableService;
import drfoliberg.common.network.Routes;
import drfoliberg.common.task.Task;
import drfoliberg.common.task.audio.AudioEncodingTask;
import drfoliberg.common.task.video.VideoEncodingTask;

public class HttpDispatcher extends RunnableService {

	private DispatcherListener listener;
	private BlockingArrayQueue<DispatchItem> items = new BlockingArrayQueue<>();
	private boolean free = true;

	public HttpDispatcher(DispatcherListener listener) {
		this.listener = listener;
	}

	public boolean isFree() {
		return free;
	}

	public void queue(DispatchItem item) {
		this.items.add(item);
	}

	private void dispatch(DispatchItem item) {
		free = false;
		Task task = item.getTask();
		Node node = item.getNode();
		String route = "";

		if (task instanceof VideoEncodingTask) {
			route = Routes.ADD_VIDEO_TASK;
		} else if (task instanceof AudioEncodingTask) {
			route = Routes.ADD_AUDIO_TASK;
		} else {
			throw new InvalidParameterException("Task must be an audio task or a video task");
		}
		boolean success = false;
		try {
			Gson gson = new Gson();
			CloseableHttpClient client = HttpClients.createDefault();
			RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000)
					.setConnectionRequestTimeout(2000).build();
			URI url = new URI("http", null, node.getNodeAddress().getHostAddress(), node.getNodePort(), route, null,
					null);
			HttpPut put = new HttpPut(url);
			put.setConfig(defaultRequestConfig);

			StringEntity entity = new StringEntity(gson.toJson(task));
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
				listener.taskAccepted(item);
			} else {
				listener.taskRefused(item);
			}
			free = true;
		}
	}

	@Override
	public void run() {
		while (!close) {
			try {
				dispatch(items.take());
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		// TODO Auto-generated method stub

	}
}