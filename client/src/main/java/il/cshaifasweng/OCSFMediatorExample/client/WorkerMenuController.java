package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class WorkerMenuController implements DataInitializable {

    private SimpleClient client;

    @FXML
    private Button updateContentButton;

    @FXML
    private Button managePriceRequestsButton;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button reportsButton; 

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
            boolean showUpdateContent = "Content manager".equalsIgnoreCase(workerType) || "Chain manager".equalsIgnoreCase(workerType) || "CinemaManager".equalsIgnoreCase(workerType);
            updateContentButton.setVisible(showUpdateContent);
            updateContentButton.setManaged(showUpdateContent);
        }
        if (managePriceRequestsButton != null) {
            boolean showManagePrice = "Chain manager".equalsIgnoreCase(workerType);
            managePriceRequestsButton.setVisible(showManagePrice);
            managePriceRequestsButton.setManaged(showManagePrice);
        }
        if (reportsButton != null) {
            boolean showReports = "CinemaManager".equalsIgnoreCase(workerType) || "Chain manager".equalsIgnoreCase(workerType);
            reportsButton.setVisible(showReports);
            reportsButton.setManaged(showReports);
        }
    }
    
    
    
    
    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        if (data instanceof Worker) {
            Worker connectedWorker = (Worker) data;
            workerType = connectedWorker.getWorkerType();
            System.out.println("Worker type after login: " + workerType); 
        }
        updateButtonVisibility();
        updateWelcomeMessage();
        System.out.println("CustomerMenuController initialized");
    }
    

    private void updateWelcomeMessage() {
        Person connectedPerson = client.getConnectedPerson();
        if (connectedPerson != null) {
            welcomeLabel.setText("Welcome, " + connectedPerson.getName());
        }
    }


    @FXML
    private void updateContent() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
        App.setRoot("UpdateContent", workerType);
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }


    @FXML
    private void handleComplaint() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
        App.setRoot("WorkerComplaintBoundary", connectedPerson);
    }

    @FXML
    private void viewReports() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
        App.setRoot("ReportsPage", connectedPerson);
    }

    @FXML
    private void handleLogout() throws IOException {
        workerType = null;
        client.logout();
        cleanup();
        App.setRoot("LoginPage", null);
    }

    @FXML
    private void managePriceRequests() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
        App.setRoot("ManagePriceRequests", connectedPerson);
    }


    public static String getWorkerType() {
        return workerType;
    }
}