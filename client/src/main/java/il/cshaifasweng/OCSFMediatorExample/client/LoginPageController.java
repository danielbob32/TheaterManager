package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class LoginPageController implements Initializable {

    private SimpleClient client = SimpleClient.getClient();

    @FXML
    private ComboBox<String> userTypeComboBox;

    @FXML
    private TextField idTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Button loginButton;

    @FXML
    private TextField timeField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("LoginPageController initialized");
        userTypeComboBox.getItems().addAll("Worker", "Customer");
        userTypeComboBox.setOnAction(this::handleUserTypeSelection);
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalTime currentTime = LocalTime.now();
            timeField.setText(currentTime.format(dtf));
        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @FXML
    void handleUserTypeSelection(ActionEvent event) {
        String selectedUserType = userTypeComboBox.getValue();
        boolean isWorker = "Worker".equals(selectedUserType);
        passwordTextField.setVisible(isWorker);
        passwordTextField.setManaged(isWorker);
    }

    @FXML
    void handleLogin(ActionEvent event) {
        System.out.println("Login button pressed");
        String userType = userTypeComboBox.getValue();
        String idText = idTextField.getText();
        String password = passwordTextField.getText();

        if (userType == null || userType.isEmpty()) {
            showAlert("Error", "Please select a user type.");
            return;
        }

        if (idText.isEmpty()) {
            showAlert("Error", "Please enter an ID.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showAlert("Error", "ID must be a number.");
            return;
        }

        if ("Worker".equals(userType) && password.isEmpty()) {
            showAlert("Error", "Please enter a password for worker login.");
            return;
        }

        System.out.println("Sending login request to server");
        if ("Worker".equals(userType)) {
            Worker worker = new Worker(id, password);
            client.tryWorkerLogin(worker);
    //                SimpleClient.getClient().sendToServer(worker);
        } else {
            Customer customer = new Customer(id);
            client.tryCustomerLogin(customer);
//            SimpleClient.getClient().sendToServer(customer);
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