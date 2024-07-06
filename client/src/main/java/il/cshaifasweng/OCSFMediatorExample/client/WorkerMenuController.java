package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.fxml.FXML;

import java.io.IOException;

public class WorkerMenuController {

    @FXML
    private void updateContent() throws IOException {
        App.setRoot("UpdateContent");
    }

    @FXML
    private void handleComplaint() throws IOException {
        App.setRoot("HandleComplaint");
    }

    @FXML
    private void viewReports() throws IOException {
        App.setRoot("ViewReports");
    }

    @FXML
    private void handleLogout() throws IOException {
        // TODO: Send logout request to server if necessary
        App.setRoot("Loginpage");
    }
}