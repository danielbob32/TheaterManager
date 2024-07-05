package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import il.cshaifasweng.OCSFMediatorExample.entities.HomeMovieLink;

public class HomeMovieLinkController {

    @FXML
    private TextField customerName;

    @FXML
    private TextField movieLink;

    @FXML
    private void handlePurchase() {
        String name = customerName.getText();
        String link = movieLink.getText();

        if (name.isEmpty() || link.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }

        HomeMovieLink homeMovieLink = new HomeMovieLink();
        homeMovieLink.setCustomerName(name);
        homeMovieLink.setMovieLink(link);

        // Send to server (you need to implement the server part)
        try {
            SimpleClient.getClient().sendToServer(homeMovieLink);
            showAlert("Success", "Home movie link purchased successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to purchase home movie link");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
