package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.entities.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;

import java.io.IOException;

public class SimpleServer extends AbstractServer {

	private ServerDB db;

	public SimpleServer(int port) {
		super(port);
		this.db = new ServerDB();
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		System.out.println("Received message from client: " + msg);
		if (msg instanceof Worker || msg instanceof Customer) {
			handleLoginRequest(msg, client);
		} else {
			String msgString = msg.toString();
			if (msgString.startsWith("#warning")) {
				Warning warning = new Warning("Warning from server!");
				try {
					client.sendToClient(warning);
					System.out.format("Sent warning to client %s\n", client.getInetAddress().getHostAddress());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void handleLoginRequest(Object loginRequest, ConnectionToClient client) {
		System.out.println("Handling login request: " + loginRequest);
		boolean loginSuccess = false;
		String message = "";

		if (loginRequest instanceof Worker) {
			Worker worker = (Worker) loginRequest;
			loginSuccess = db.checkWorkerCredentials(worker.getId(), worker.getPassword());
			message = loginSuccess ? "Worker login successful" : "Invalid worker credentials";
		} else if (loginRequest instanceof Customer) {
			Customer customer = (Customer) loginRequest;
			loginSuccess = db.checkCustomerCredentials(customer.getId());
			message = loginSuccess ? "Customer login successful" : "Invalid customer ID";
		}

		try {
			client.sendToClient(new Warning(message));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void serverStopped() {
		super.serverStopped();
		db.close();
	}

	@Override
	protected void serverClosed() {
		super.serverClosed();
		db.close();
	}
}