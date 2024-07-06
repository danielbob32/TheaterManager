package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;

public class SimpleClient extends AbstractClient {

	private static SimpleClient client = null;

	private SimpleClient(String host, int port) {
		super(host, port);
	}

	@Override
	protected void handleMessageFromServer(Object msg) {
		System.out.println("Received message from server: " + msg);
		if (msg instanceof Warning) {
			Warning warning = (Warning) msg;
			String message = warning.getMessage();
			System.out.println("Warning message content: " + message);

			if (message.contains("Customer login successful")) {
				Platform.runLater(() -> {
					try {
						App.setRoot("CustomerMenu");
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("Failed to load CustomerMenu: " + e.getMessage());
					}
				});
			} else if (message.contains("Worker login successful")) {
				Platform.runLater(() -> {
					try {
						App.setRoot("WorkerMenu");
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("Failed to load WorkerMenu: " + e.getMessage());
					}
				});
			} else {
				// Handle login failure
				Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Login Failed");
					alert.setHeaderText(null);
					alert.setContentText(message);
					alert.showAndWait();
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