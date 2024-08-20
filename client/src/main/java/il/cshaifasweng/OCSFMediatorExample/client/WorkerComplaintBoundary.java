package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.events.ComplaintListEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.RespondToComplaintEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Complaint;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class WorkerComplaintBoundary implements DataInitializable {
    @FXML
    private TableView<Complaint> complaintTable;
    @FXML
    private TableColumn<Complaint, Integer> complaintIdColumn;
    @FXML
    private TableColumn<Complaint, String> titleColumn;
    @FXML
    private TableColumn<Complaint, String> dateColumn;
    @FXML
    private TableColumn<Complaint, String> descriptionColumn;
    @FXML
    private TableColumn<Complaint, Boolean> isActiveColumn;
    @FXML
    private TableColumn<Complaint, Integer> refundColumn;
    @FXML
    private TableColumn<Complaint, String> responseColumn;
    @FXML
    private TableColumn<Complaint, Integer> customerIdColumn;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField responseField;
    @FXML
    private TextField refundField;  // New field for refund amount
    @FXML
    private Label responseLabel;    // New label for response field
    @FXML
    private Button selectComplaintButton;
    @FXML
    private Button submitReplyButton;
    @FXML
    private Button backButton;
    @FXML
    private CheckBox hideNonActiveCheckBox;
    @FXML
    private TableColumn<Complaint, String> cinemaColumn;

    private Complaint selectedComplaint;
    private SimpleClient client;
    private List<Complaint> allComplaints;

    public WorkerComplaintBoundary() {
    }

    @FXML
    public void initialize() throws IOException {
        EventBus.getDefault().register(this);

        complaintTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        complaintIdColumn.setCellValueFactory(new PropertyValueFactory<>("complaint_id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        isActiveColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isActive()));
        refundColumn.setCellValueFactory(new PropertyValueFactory<>("refund"));
        responseColumn.setCellValueFactory(new PropertyValueFactory<>("response"));
        customerIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCustomer().getPersonId()).asObject());
        cinemaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCinemaName() != null ? cellData.getValue().getCinemaName() : "N/A"));


        hideNonActiveCheckBox.setOnAction(event -> filterComplaints());
    }

    @Override
    public void initData(Object data) {
        System.out.println("In Worker Complaint Boundary initData");
        try {
            loadComplaints();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    private void loadComplaints() throws IOException {
        client.fetchAllComplaints();
    }

    @Subscribe
    public void onComplaintListEvent(ComplaintListEvent event) {
        System.out.println("DEBUG: in onComplaintListEvent");
        allComplaints = event.getComplaints();
        displayComplaints(allComplaints);
    }

    @Subscribe
    public void onRespondToComplaintEvent(RespondToComplaintEvent event) {
        Platform.runLater(() -> {
            showAlert("Success", "Reply submitted successfully!");
            try {
                loadComplaints();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void displayComplaints(List<Complaint> complaints) {
        Platform.runLater(() -> {
            complaintTable.getItems().clear();
            complaintTable.getItems().addAll(complaints);
        });
    }

    private void filterComplaints() {
        boolean hideNonActive = hideNonActiveCheckBox.isSelected();
        List<Complaint> filteredComplaints = allComplaints.stream()
                .filter(complaint -> !hideNonActive || complaint.isActive())
                .collect(Collectors.toList());
        displayComplaints(filteredComplaints);
    }

    @FXML
    void handleSelectComplaint(ActionEvent event) {
        selectedComplaint = complaintTable.getSelectionModel().getSelectedItem();
        if (selectedComplaint != null) {
            descriptionField.setText(selectedComplaint.getDescription());
        } else {
            showAlert("Error", "Please select a complaint from the table.");
        }
    }

    @FXML
    void handleSubmitReply(ActionEvent event) throws IOException {
        if (selectedComplaint != null) {
            String response = responseField.getText();
            if (response.isEmpty()) {
                showAlert("Error", "Response field cannot be empty.");
                return;
            }

            // Check if the complaint already has a reply
            if (!selectedComplaint.isActive()) {
                showAlert("Error", "A reply has already been submitted for this complaint.");
                return;
            }

            // Check for refund value if applicable
            String refundText = refundField.getText();
            try {
                int refundAmount = Integer.parseInt(refundText);
                selectedComplaint.setResponse(response);
                selectedComplaint.setRefund(refundAmount);
                selectedComplaint.setActive(false);
                client.respondToComplaint(selectedComplaint);
                responseField.clear();
                refundField.clear();
                selectedComplaint = null;
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a numeric value for the refund amount.");
            }
        } else {
            showAlert("Error", "Please select a complaint first.");
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBackButton(ActionEvent event) throws IOException {
        cleanup();
        App.setRoot("WorkerMenu", null);
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}
