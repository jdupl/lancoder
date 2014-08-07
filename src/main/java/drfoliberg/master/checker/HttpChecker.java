package drfoliberg.master.checker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.io.Charsets;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.util.BlockingArrayQueue;

import com.google.gson.Gson;

import drfoliberg.common.Node;
import drfoliberg.common.RunnableService;
import drfoliberg.common.network.Routes;
import drfoliberg.common.network.messages.cluster.StatusReport;

public class HttpChecker extends RunnableService implements Comparable<HttpChecker> {

	private NodeCheckerListener listener;
	final BlockingQueue<Node> tasks = new BlockingArrayQueue<Node>(100);

	public HttpChecker(NodeCheckerListener listener) {
		this.listener = listener;
	}

	public synchronized boolean add(Node n) {
		return this.tasks.offer(n);
	}

	public int getQueueSize() {
		return this.tasks.size();
	}

	public void checkNode(Node n) {
		try {
			Gson gson = new Gson();
			CloseableHttpClient client = HttpClients.createDefault();
			RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000)
					.setConnectionRequestTimeout(2000).build();
			URI url = new URI("http", null, n.getNodeAddress().getHostAddress(), n.getNodePort(), Routes.NODE_STATUS,
					null, null);
			HttpGet get = new HttpGet(url);
			get.setConfig(defaultRequestConfig);
			CloseableHttpResponse response = client.execute(get);
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream is = response.getEntity().getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
				StatusReport report = gson.fromJson(br, StatusReport.class);
				listener.readStatusReport(report);
			} else {
				System.err.printf("Node responded with bad status: %d\n", response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			listener.nodeDisconnected(n);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (!close) {
			try {
				Node next = tasks.take();
				checkNode(next);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void serviceFailure(Exception e) {
		// TODO Auto-generated method stub
	}

	@Override
	public int compareTo(HttpChecker o) {
		return Integer.compare(getQueueSize(), o.getQueueSize());
	}
}
