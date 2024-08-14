package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.regex.Pattern;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import javafx.application.Platform;
import il.cshaifasweng.OCSFMediatorExample.client.events.*;

public class PurchaseLinkBoundary implements DataInitializable {

    @FXML private Label movieTitleLabel;
    @FXML private Label selectedDateLabel;
    @FXML private Label selectedTimeLabel;
    @FXML private Label totalPriceLabel;

    @FXML private TextField nameField;
    @FXML private TextField idField;
    @FXML private TextField emailField;
    @FXML private TextField creditCardNumberField;

    @FXML private Button confirmPaymentButton;
    @FXML private Button backButton;

    private SimpleClient client;
    private Movie currentMovie;
    private String selectedDate;
    private String selectedTime;
    private int totalPrice; // Example price for the home movie link

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        if (data instanceof Object[]) {
            Object[] params = (Object[]) data;
            currentMovie = (Movie) params[0];
            selectedTime = (String) params[1];
            selectedDate = (String) params[2];
            initializePaymentDetails();
        }
    }

    private void initializePaymentDetails() {
        movieTitleLabel.setText(currentMovie.getEnglishName());
        selectedDateLabel.setText("Date: " + selectedDate);
        selectedTimeLabel.setText("Time: " + selectedTime);
        totalPriceLabel.setText("Price: ₪" + totalPrice);

        Person connectedPerson = client.getConnectedPerson();
        if (connectedPerson instanceof Customer) {
            nameField.setText(connectedPerson.getName());
            idField.setText(String.valueOf(connectedPerson.getPersonId()));
            emailField.setText(((Customer) connectedPerson).getEmail());
        }
    }

    @FXML
    private void initialize() {
        EventBus.getDefault().register(this);
    }

    @FXML
    private void handleConfirmPayment() {
        if (!validateInput()) {
            return;
        }

        try {
            String name = nameField.getText().trim();
            String id = idField.getText().trim();
            String email = emailField.getText().trim();
            String creditCard = creditCardNumberField.getText().trim();

            client.purchaseLink(
                currentMovie.getId(),
                selectedDate,
                selectedTime,
                totalPrice=50,
                email,
                name,
                id,
                creditCard
            );
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error processing payment. Please try again.");
        }
    }

    private boolean validateInput() {
        return validateName() && validateId() && validateEmail() && validateCreditCard();
    }

    private boolean validateName() {
        String name = nameField.getText().trim();
        if (!Pattern.matches("[a-zA-Z\\s]+", name)) {
            showAlert("Invalid name. Please use only letters and spaces.");
            return false;
        }
        return true;
    }

    private boolean validateId() {
        String id = idField.getText().trim();
        if (!Pattern.matches("\\d{9}", id)) {
            showAlert("Invalid ID. Please enter 9 digits.");
            return false;
        }
        return true;
    }

    private boolean validateEmail() {
        String email = emailField.getText().trim();
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!Pattern.matches(emailRegex, email)) {
            showAlert("Invalid email format.");
            return false;
        }
        return true;
    }

    private boolean validateCreditCard() {
        String creditCard = creditCardNumberField.getText().trim();
        if (!Pattern.matches("\\d{10}", creditCard)) {
            showAlert("Invalid credit card number. Please enter 10 digits.");
            return false;
        }
        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBackButton() {
        try {
            cleanup();
            App.setRoot("HomeMovieDetails", currentMovie);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error returning to movie details.");
        }
    }

    @Subscribe
    public void onPurchaseResponse(PurchaseResponseEvent event) {
            Platform.runLater(() -> {
                if (event.isSuccess()) {
                    System.out.println("Purchase successful. Response data: " + event.getData());
                    showAlert("Payment successful! An email has been sent with the movie link details.");
                    try {
                        App.setRoot("LinkDetails", event.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert("Error navigating to link details.");
                    }
                } else {
                    showAlert("Purchase failed: " + event.getMessage());
                }
            });
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}


