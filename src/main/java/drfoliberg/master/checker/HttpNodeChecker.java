package main.java.drfoliberg.master.checker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import main.java.drfoliberg.common.Node;
import main.java.drfoliberg.common.Service;
import main.java.drfoliberg.common.network.Routes;
import main.java.drfoliberg.common.network.messages.cluster.StatusReport;

import org.apache.commons.io.Charsets;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;

public class HttpNodeChecker extends Service {

	private final static int MS_DELAY_BETWEEN_CHECKS = 5000;
	private NodeCheckerListener listener;

	public HttpNodeChecker(NodeCheckerListener listener) {
		this.listener = listener;
	}

	private boolean checkNodes() {
		if (listener.getNodes().size() == 0) {
			System.out.println("MASTER NODE CHECKER: no nodes to check!");
			return false;
		}
		System.out.println("MASTER NODE CHECKER: checking if nodes are still alive");
		for (Node n : listener.getOnlineNodes()) {
			checkNode(n);
		}
		return false;
	}

	private void checkNode(Node n) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("Starting node checker service!");
		while (!close) {
			try {
				checkNodes();
				System.out.println("NODE CHECKER: checking back in 5 seconds");
				Thread.currentThread();
				Thread.sleep(MS_DELAY_BETWEEN_CHECKS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Closed node checker service!");
	}

}
