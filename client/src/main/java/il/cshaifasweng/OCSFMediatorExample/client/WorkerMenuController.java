package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class WorkerMenuController {

    @FXML
    private Button updateContentButton;

    private String workerType;

    public void initialize() {
        // This method is called after the FXML file has been loaded
        updateButtonVisibility();
    }

    public void setWorkerType(String workerType) {
        this.workerType = workerType;
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (updateContentButton != null) {
            updateContentButton.setVisible("Content manager".equals(workerType));
            updateContentButton.setManaged("Content manager".equals(workerType));
        }
    }


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