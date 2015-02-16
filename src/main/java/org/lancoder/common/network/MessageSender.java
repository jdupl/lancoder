package org.lancoder.common.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.lancoder.common.network.cluster.messages.Message;

public class MessageSender {

	private static final int DEFAULT_TIMEOUT_MSEC = 3000;

	public static Message sendWithExceptions(Message toSend, InetAddress address, int port) throws IOException,
			ClassNotFoundException {
		return sendWithExceptions(toSend, address, port, DEFAULT_TIMEOUT_MSEC);
	}

	public static Message sendWithExceptions(Message toSend, InetAddress address, int port, int timeout)
			throws IOException, ClassNotFoundException {
		Message received = null;
		SocketAddress socketAddress = new InetSocketAddress(address, port);

		try (Socket s = new Socket()) {
			s.setSoTimeout(timeout);
			s.connect(socketAddress, timeout);

			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			out.flush();

			out.writeObject(toSend);
			out.flush();

			Object o = in.readObject();
			if (o instanceof Message) {
				received = (Message) o;
			}
		} catch (Exception e) {
			// close socket
			throw e;
		}
		return received;
	}

	public static Message send(Message toSend, InetAddress address, int port) {
		return send(toSend, address, port, DEFAULT_TIMEOUT_MSEC);
	}

	public static Message send(Message toSend, InetAddress address, int port, int timeout) {
		Message received = null;
		SocketAddress socketAddress = new InetSocketAddress(address, port);

		try (Socket s = new Socket()) {
			s.setSoTimeout(timeout);
			s.connect(socketAddress, timeout);

			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			out.flush();

			out.writeObject(toSend);
			out.flush();

			Object o = in.readObject();
			if (o instanceof Message) {
				received = (Message) o;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return received;
	}
}
