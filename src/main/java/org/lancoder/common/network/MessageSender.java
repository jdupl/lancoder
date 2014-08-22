package org.lancoder.common.network;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.http.HttpMethod;
import org.lancoder.common.Node;
import org.lancoder.common.network.messages.cluster.Message;

public class MessageSender {

	private final static int DEFAULT_TIMEOUT = 2000;

	private static URI getUri(Message m, Node n) throws URISyntaxException {
		return new URI("http", null, n.getNodeAddress().getHostAddress(), n.getNodePort(), m.getPath(), null, null);
	}

	private static RequestConfig getConfig(int timeout) {
		return RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout).build();
	}

	private static HttpRequestBase getHttpEntity(HttpMethod method) {
		switch (method) {
		case DELETE:
			return new HttpDelete();
		case PUT:
			return new HttpPut();
		case POST:
			return new HttpPost();
		case GET:
			return new HttpGet();
		default:
			// Yes the code will break.
			return null;
		}
	}

	private static HttpResponse send(Message m, Node n, int timeout, HttpMethod method) {
		HttpResponse r = null;
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpRequestBase base = getHttpEntity(method);
			base.setURI(getUri(m, n));
			base.setConfig(getConfig(timeout));
			r = client.execute(base);
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		return r;
	}

	public HttpResponse put(Message m, Node n) {
		return send(m, n, DEFAULT_TIMEOUT, HttpMethod.PUT);
	}

	public HttpResponse get(Message m, Node n) {
		return send(m, n, DEFAULT_TIMEOUT, HttpMethod.GET);
	}

	public HttpResponse post(Message m, Node n) {
		return send(m, n, DEFAULT_TIMEOUT, HttpMethod.POST);
	}

	public HttpResponse delete(Message m, Node n) {
		return send(m, n, DEFAULT_TIMEOUT, HttpMethod.DELETE);
	}

}
