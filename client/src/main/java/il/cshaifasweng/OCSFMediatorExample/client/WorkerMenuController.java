package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class WorkerMenuController {

    @FXML
    private Button updateContentButton;

    public static String workerType;

    public void initialize() {
        updateButtonVisibility();
    }

    public void setWorkerType(String type) {
        workerType = type;
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
        workerType = null;
        App.setRoot("Loginpage");
    }

    public static String getWorkerType() {
        return workerType;
    }
}