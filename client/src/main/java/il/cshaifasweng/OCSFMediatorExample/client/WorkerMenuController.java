package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class WorkerMenuController implements DataInitializable {

    private SimpleClient client;

    @FXML
    private Button updateContentButton;

    @FXML
    private Button managePriceRequestsButton;

    public static String workerType;

    public void initialize() {
    }

    public void setWorkerType(String type) {
        System.out.println("Setting worker type to " + type);
        workerType = type;
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (updateContentButton != null) {
            updateContentButton.setVisible("Content manager".equals(workerType) || "Chain manager".equals(workerType));
            updateContentButton.setManaged("Content manager".equals(workerType) || "Chain manager".equals(workerType));
        }
        if (managePriceRequestsButton != null) {
            managePriceRequestsButton.setVisible("Chain manager".equals(workerType));
            managePriceRequestsButton.setManaged("Chain manager".equals(workerType));
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
        }
        updateButtonVisibility();
        System.out.println("CustomerMenuController initialized");
    }


    @FXML
    private void updateContent() throws IOException {
        Person connectedPerson = client.getConnectedPerson();

        App.setRoot("UpdateContent", workerType);
    }

    @FXML
    private void handleComplaint() throws IOException {
        Person connectedPerson = client.getConnectedPerson();

        App.setRoot("HandleComplaint", connectedPerson);
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

    @FXML
    private void managePriceRequests() throws IOException {
        Person connectedPerson = client.getConnectedPerson();

        App.setRoot("ManagePriceRequests", connectedPerson);
    }

    public static String getWorkerType() {
        return workerType;
    }
}