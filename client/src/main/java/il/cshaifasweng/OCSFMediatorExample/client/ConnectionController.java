package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class ConnectionController {

    @FXML private TextField ipTextField;
    @FXML private TextField portTextField;
    @FXML private Label welcomeLabel;
    @FXML private Button connectButton;
    @FXML private VBox mainContainer;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to The Movie Theater System");
        ipTextField.setPromptText("Enter host url (Default: 'localhost')");
        portTextField.setPromptText("Enter port number (Default: '3000')");

        // Add animation for the welcome message
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), welcomeLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(1.5), welcomeLabel);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1.0);
        scale.setToY(1.0);

        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scale);
        parallelTransition.play();

        // Add hover effect for the connect button
        connectButton.setOnMouseEntered(e -> connectButton.setStyle("-fx-background-color: #2a5298;"));
        connectButton.setOnMouseExited(e -> connectButton.setStyle("-fx-background-color: #1e3c72;"));
    }

    @FXML
    void handleConnect() {
        String ip = ipTextField.getText().isEmpty() ? "localhost" : ipTextField.getText();
        String portText = portTextField.getText().isEmpty() ? "3000" : portTextField.getText();
        int port;

        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            showAlert("Error", "Port must be a number.");
            return;
        }

        try {
            SimpleClient client = SimpleClient.getClient(ip, port);
            App.setClient(client);
            client.openConnection();

            // Animate the transition
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), mainContainer);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                try {
                    App.setRoot("LoginPage", null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fadeOut.play();

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