package drfoliberg.worker.contacter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import drfoliberg.common.RunnableService;
import drfoliberg.common.network.Routes;
import drfoliberg.common.network.messages.cluster.ConnectMessage;

import org.apache.commons.io.Charsets;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;

@Deprecated
public class ContactMasterHttp extends RunnableService {

	ConctactMasterListener listener;
	InetAddress masterAddress;
	int masterPort;

	public ContactMasterHttp(InetAddress masterAddress, int masterPort, ConctactMasterListener listener) {
		this.listener = listener;
		this.masterAddress = masterAddress;
		this.masterPort = masterPort;
	}

	private void contactMaster() {
		try {
			System.err.println("Trying to contact...");
			CloseableHttpClient client = HttpClients.createDefault();
			RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(2000).build();

			// build url
			URI url = new URI("http", null, masterAddress.getHostAddress(), masterPort, Routes.CONNECT_NODE, null, null);
			HttpPost post = new HttpPost(url);
			post.setConfig(defaultRequestConfig);

			// build connect message and http json entity
			ConnectMessage m = new ConnectMessage(listener.getCurrentNodeUnid(), listener.getCurrentNodePort(),
					listener.getCurrentNodeName(), listener.getCurrentNodeAddress());
			Gson gson = new Gson();
			StringEntity entity = new StringEntity(gson.toJson(m));
			entity.setContentEncoding(Charsets.UTF_8.toString());
			entity.setContentType(ContentType.APPLICATION_JSON.toString());
			post.setEntity(entity);

			// Send request and read response
			CloseableHttpResponse response = client.execute(post);
			if (response.getStatusLine().getStatusCode() == 200) {
				String unid = null;
				try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
						Charsets.UTF_8))) {
					unid = br.readLine();
				}

				if (unid != null && !unid.isEmpty()) {
					// this will trigger a node status change that will then stop this service
					listener.receivedUnid(unid);
				} else {
					System.err.println("Received null string or invalid string from master ?");
				}
			} else {
				System.err.println("Contacter: Got bad response code from master: "
						+ response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			// timeout
			System.err.println("Failed to contact master.");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (!close) {
			try {
				contactMaster();
				// sleep 10 times 500ms for fast closing
				// TODO better way to sleep
				for (int i = 0; i < 10; i++) {
					if (close)
						break;
					Thread.currentThread();
					Thread.sleep(500);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Worker contacter closed");
	}

	@Override
	public void serviceFailure(Exception e) {
		// TODO
	}
}
