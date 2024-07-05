package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import il.cshaifasweng.OCSFMediatorExample.entities.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;

import java.net.URL;
import java.util.ResourceBundle;
import java.io.IOException;

public class LoginPageController implements Initializable {

    @FXML
    private ComboBox<String> userTypeComboBox;

    @FXML
    private TextField idTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Button loginButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the ComboBox with user types
        userTypeComboBox.getItems().addAll("Worker", "Customer");
        userTypeComboBox.setOnAction(this::handleUserTypeSelection);

        // Initially hide the password field
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);
    }

    @FXML
    void handleUserTypeSelection(ActionEvent event) {
        String selectedUserType = userTypeComboBox.getValue();
        boolean isWorker = "Worker".equals(selectedUserType);

        // Show/hide password field based on user type
        passwordTextField.setVisible(isWorker);
        passwordTextField.setManaged(isWorker);
    }

    @FXML
    void handleLogin(ActionEvent event) {
        System.out.println("Login button clicked");
        String userType = userTypeComboBox.getValue();
        String idText = idTextField.getText();
        String password = passwordTextField.getText();

        // Validate input
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

        // Create and send login request
        try {
            if ("Worker".equals(userType)) {
                Worker worker = new Worker(id, password);
                SimpleClient.getClient().sendToServer(worker);
            } else {
                Customer customer = new Customer(id);
                SimpleClient.getClient().sendToServer(customer);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not send login request to server.");
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