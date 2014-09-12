package org.lancoder.worker.contacter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.lancoder.common.RunnableService;

public class ContactMasterObject extends RunnableService {

	private ConctactMasterListener listener;
	private InetAddress masterAddress;
	private int masterPort;

	public ContactMasterObject(InetAddress masterAddress, int masterPort, ConctactMasterListener listener) {
		this.listener = listener;
		this.masterAddress = masterAddress;
		this.masterPort = masterPort;
	}

	private void contactMaster() {
		System.err.println("Trying to contact master...");
		InetSocketAddress addr = new InetSocketAddress(masterAddress, masterPort);
		try (Socket s = new Socket()) {
			s.setSoTimeout(2000);
			s.connect(addr);
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			out.writeObject(listener.getConnectMessage());
			out.flush();
			Object res = in.readObject();
			if (res instanceof String) {
				String unid = (String) res;
				if (unid != null && !unid.isEmpty()) {
					// this will trigger a node status change that will then stop this service
					listener.receivedUnid(unid);
				} else {
					System.err.println("Received null string or invalid string from master ?");
				}
			}
			s.close();
		} catch (IOException e) {
			System.err.println("Failed to contact master.");
		} catch (ClassNotFoundException e) {
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
		// TODO Auto-generated method stub

	}

}
