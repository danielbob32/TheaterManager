package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import javafx.application.Platform;

import java.io.IOException;

public class SimpleClient extends AbstractClient {

	private static SimpleClient client = null;

	private SimpleClient(String host, int port) {
		super(host, port);
	}

	@Override
	protected void handleMessageFromServer(Object msg) {
		if (msg instanceof Warning) {
			Warning warning = (Warning) msg;
			String message = warning.getMessage();
			System.out.println("Warning received: " + message);

			if (message.startsWith("Worker login successful")) {
				String[] parts = message.split(":");
				if (parts.length == 2) {
					String workerType = parts[1].trim();
					App.setWorkerType(workerType);
					Platform.runLater(() -> {
						try {
							App.setRoot("WorkerMenu");
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				}
			} else if (message.equals("Customer login successful")) {
				Platform.runLater(() -> {
					try {
						App.setRoot("CustomerMenu");
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			} else {
				// Handle login failure
				Platform.runLater(() -> {
					// Show error message
				});
			}
		}
	}

	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);
		}
		return client;
	}

	public void sendToServer(Object msg) throws IOException {
		System.out.println("Sending to server: " + msg);
		super.sendToServer(msg);
	}

	public void openConnection() throws IOException {
		System.out.println("Opening connection to server");
		super.openConnection();
	}
}