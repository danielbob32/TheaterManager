package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ConnectionController {

    @FXML
    private TextField ipTextField;

    @FXML
    private TextField portTextField;

    @FXML
    void handleConnect() {
        String ip = ipTextField.getText();
        String portText = portTextField.getText();
        int port;

        if (ip.isEmpty() || portText.isEmpty()) {
            showAlert("Error", "IP address and port must not be empty.");
            return;
        }

        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            showAlert("Error", "Port must be a number.");
            return;
        }

        try {
            SimpleClient client = SimpleClient.getClient(ip, port);
            App.setClient(client);  // Add this method to your App class to set the client
            client.openConnection();

            // Proceed to the login page
            App.setRoot("Loginpage", null);
        } catch (IOException e) {
            showAlert("Connection Failed", "Could not connect to server.");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
