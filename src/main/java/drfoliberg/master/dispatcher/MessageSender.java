package main.java.drfoliberg.master.dispatcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import main.java.drfoliberg.common.Node;
import main.java.drfoliberg.common.network.messages.cluster.Message;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class MessageSender {

	public static HttpResponse send(Message m, Node n) {
		return send(m, n, 2000);
	}

	public static HttpResponse send(Message m, Node n, int timeout) {
		HttpResponse r = null;
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(timeout).build();

			URI url = new URI("http", null, n.getNodeAddress().getHostAddress(), n.getNodePort(), m.getPath(), null,
					null);
			HttpPost post = new HttpPost(url);
			post.setConfig(defaultRequestConfig);
			r = client.execute(post);
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		return r;
	}
}
