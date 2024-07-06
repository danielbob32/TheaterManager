package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;

public class SimpleServer extends AbstractServer {

	private ServerDB db;

	public SimpleServer(int port) {
		super(port);
		try {
			this.db = new ServerDB();
		} catch (Exception e) {
			System.err.println("Failed to initialize ServerDB: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		System.out.println("Received message from client: " + msg);
		if (msg instanceof Worker || msg instanceof Customer) {
			handleLoginRequest(msg, client);
		} else {
			System.out.println("Received unknown message type: " + msg.getClass().getName());
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
		boolean loginSuccess = false;
		String message = "";
		try {
			if (loginRequest instanceof Worker) {
				Worker worker = (Worker) loginRequest;
				loginSuccess = db.checkWorkerCredentials(worker.getId(), worker.getPassword());
				if (loginSuccess) {
					String workerType = db.getWorkerType(worker.getId());
					message = "Worker login successful: " + workerType;
				} else {
					message = "Invalid worker credentials";
				}
			} else if (loginRequest instanceof Customer) {
				Customer customer = (Customer) loginRequest;
				loginSuccess = db.checkCustomerCredentials(customer.getId());
				message = loginSuccess ? "Customer login successful" : "Invalid customer ID";
			} else {
				message = "Invalid login request type";
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = "An error occurred during login: " + e.getMessage();
		}

		try {
			Warning warning = new Warning(message);
			client.sendToClient(warning);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void serverStarted() {
		super.serverStarted();
		System.out.println("Server started");
	}

	@Override
	protected void serverStopped() {
		super.serverStopped();
		System.out.println("Server stopped");
		db.close();
	}

	@Override
	protected void serverClosed() {
		super.serverClosed();
		System.out.println("Server closed");
		db.close();
	}
}