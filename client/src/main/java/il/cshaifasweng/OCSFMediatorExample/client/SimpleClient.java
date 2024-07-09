package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;


public class SimpleClient extends AbstractClient {

	private static SimpleClient client = null;
	private ObjectMapper objectMapper = new ObjectMapper();

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
			} else if (message.equals("Movie have been added successfully") || message.equals("Home movie have been added successfully")) {
				Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Success");
					alert.setHeaderText(null);
					alert.setContentText(message);
					alert.showAndWait();
				});
			} else {
				Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Error");
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

	public void tryWorkerLogin(Worker worker)
	{
		try {
			sendToServer(new Message(0, "login:worker", serializeWorker(worker)));
		} catch (IOException e) {
			e.printStackTrace();
//			EventBus.getDefault().post(new UpdateFailureEvent("Failed to update movie show times"));
			System.out.println("Failed to update movie show times");
		}
	}

	public void tryCustomerLogin(Customer customer)
	{
		try {
			sendToServer(new Message(0, "login:customer", objectMapper.writeValueAsString(customer)));
		} catch (IOException e) {
			e.printStackTrace();
//			EventBus.getDefault().post(new UpdateFailureEvent("Failed to update movie show times"));
			System.out.println("Failed to recognize this customer");
		}
	}

	public String serializeWorker(Worker worker)
	{
		try {
			return objectMapper.writeValueAsString(worker);
		} catch (IOException e) {
			e.printStackTrace();
//			EventBus.getDefault().post(new UpdateFailureEvent("Failed to serialize movie"));
			System.out.println("couldn't serialize worker");
			return null;
		}
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