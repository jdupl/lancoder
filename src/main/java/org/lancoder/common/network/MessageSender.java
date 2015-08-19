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
		SocketAddress socketAddress = new InetSocketAddress(address, port);

		try (Socket s = new Socket()) {
			return send(toSend, timeout, socketAddress, s);
		} catch (Exception e) {
			throw e;
		}
	}

	public static Message send(Message toSend, InetAddress address, int port) {
		return send(toSend, address, port, DEFAULT_TIMEOUT_MSEC);
	}

	/**
	 * Sends message to address and port. Catches all exceptions. Will return null reponse if error occured.
	 *
	 * @param toSend
	 *            The message to send
	 * @param address
	 *            The address to send to
	 * @param port
	 *            The port to send to
	 * @param timeout
	 *            The connect and send timeout
	 * @return Response or null if error occured
	 */
	public static Message send(Message toSend, InetAddress address, int port, int timeout) {
		Message response = null;
		SocketAddress socketAddress = new InetSocketAddress(address, port);

		try (Socket s = new Socket()) {
			response = send(toSend, timeout, socketAddress, s);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return response;
	}


	private static Message send(Message toSend, int timeout, SocketAddress socketAddress, Socket s)
			throws IOException, ClassNotFoundException {
		Message response = null;

		s.setSoTimeout(timeout);
		s.connect(socketAddress, timeout);

		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(s.getInputStream());
		out.flush();

		out.writeObject(toSend);
		out.flush();

		Object o = in.readObject();
		if (o instanceof Message) {
			response = (Message) o;
		}
		return response;
	}
}
