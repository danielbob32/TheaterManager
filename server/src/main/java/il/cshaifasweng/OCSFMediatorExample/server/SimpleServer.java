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
		System.out.println("Handling login request: " + loginRequest);
		boolean loginSuccess = false;
		String message = "";
		try {
			if (loginRequest instanceof Worker) {
				Worker worker = (Worker) loginRequest;
				System.out.println("Checking worker credentials for ID: " + worker.getId());
				loginSuccess = db.checkWorkerCredentials(worker.getId(), worker.getPassword());
				message = loginSuccess ? "Worker login successful" : "Invalid worker credentials";
			} else if (loginRequest instanceof Customer) {
				Customer customer = (Customer) loginRequest;
				System.out.println("Checking customer credentials for ID: " + customer.getId());
				loginSuccess = db.checkCustomerCredentials(customer.getId());
				message = loginSuccess ? "Customer login successful" : "Invalid customer ID";
			} else {
				message = "Invalid login request type: " + loginRequest.getClass().getName();
				System.out.println(message);
			}
		} catch (Exception e) {
			System.err.println("Error during login process: " + e.getMessage());
			e.printStackTrace();
			message = "An error occurred during login: " + e.getMessage();
		}

		System.out.println("Login result: " + message);
		try {
			Warning warning = new Warning(message);
			client.sendToClient(warning);
			System.out.println("Sent response to client: " + message);
		} catch (IOException e) {
			System.err.println("Error sending response to client: " + e.getMessage());
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