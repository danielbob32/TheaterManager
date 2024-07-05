package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.Warning;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.greenrobot.eventbus.EventBus;

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

			if (message.contains("login successful")) {
				// Handle successful login
				Platform.runLater(() -> {
					System.out.println("Login successful");
					// TODO: Switch to main application screen
				});
			} else if (message.contains("Invalid") || message.contains("error")) {
				// Handle failed login
				Platform.runLater(() -> {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Login Failed");
					alert.setHeaderText(null);
					alert.setContentText(message);
					alert.showAndWait();
				});
			} else {
				// Handle other warnings
				EventBus.getDefault().post(new WarningEvent(warning));
			}
		}
	}

	
	public static SimpleClient getClient() {
		if (client == null) {
			client = new SimpleClient("localhost", 3000);
		}
		return client;
	}

}
