package org.lancoder.common.network.cluster;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownServiceException;

import org.eclipse.jetty.http.HttpTester.Message;

public class Messager {

	private final static int DEFAULT_TIMEOUT_MSEC = 2000;

	public static Message send(InetSocketAddress addr, Message m) throws ClassNotFoundException, IOException {
		try (Socket s = new Socket()) {
			s.setSoTimeout(DEFAULT_TIMEOUT_MSEC);
			s.connect(addr);
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			out.writeObject(m);
			out.flush();
			Object res = in.readObject();
			if (res instanceof Message) {
				return (Message) res;
			} else {
				throw new UnknownServiceException(String.format("Response from %s is not a valid message.",
						addr.getAddress()));
			}
		}
	}
}
