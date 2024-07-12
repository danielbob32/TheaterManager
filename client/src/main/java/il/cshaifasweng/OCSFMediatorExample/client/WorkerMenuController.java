package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class WorkerMenuController implements DataInitializable {

    private SimpleClient client;

    @FXML
    private Button updateContentButton;

    public static String workerType;

    public void initialize() {
        //updateButtonVisibility();
    }

    public void setWorkerType(String type) {
        System.out.println("Setting worker type to " + type);
        workerType = type;
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (updateContentButton != null) {
            updateContentButton.setVisible("Content manager".equals(workerType));
            updateContentButton.setManaged("Content manager".equals(workerType));
        }
    }

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        if (data instanceof String) {
            workerType = (String) data;
            System.out.println("1111Setting worker type to " + data);
            updateButtonVisibility();
        }
        System.out.println("CustomerMenuController initialized");
    }


    @FXML
    private void updateContent() throws IOException {
        App.setRoot("UpdateContent", null);
    }

    @FXML
    private void handleComplaint() throws IOException {
        App.setRoot("HandleComplaint", null);
    }

    @FXML
    private void viewReports() throws IOException {
        App.setRoot("ViewReports", null);
    }

    @FXML
    private void handleLogout() throws IOException {
        workerType = null;
        App.setRoot("Loginpage", null);
    }

    public static String getWorkerType() {
        return workerType;
    }
}