package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.events.CinemaListEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.CustomerComplaintListEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.RespondToComplaintEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.SubmitComplaintEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Cinema;
import il.cshaifasweng.OCSFMediatorExample.entities.Complaint;
import il.cshaifasweng.OCSFMediatorExample.entities.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomerComplaintBoundary implements DataInitializable {

    @FXML
    private TableView<Complaint> complaintTableView;

    @FXML
    private TableColumn<Complaint, String> titleColumn;

    @FXML
    private TableColumn<Complaint, String> dateColumn;

    @FXML
    private TableColumn<Complaint, String> statusColumn;

    @FXML
    private Button viewComplaintButton;

    @FXML
    private Button submitNewComplaintButton;

    @FXML
    private TableColumn<Complaint, String> cinemaColumn;

    private ComboBox<String> cinemaComboBox; // Add this line

    private SimpleClient client;

    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);

        // Initialize cinemaComboBox
        cinemaComboBox = new ComboBox<>();

        complaintTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Set up the table columns
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate().toString()));
        statusColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Resolved"));
        cinemaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCinemaName() != null ? cellData.getValue().getCinemaName() : "N/A"));

        // Handle button actions
        viewComplaintButton.setOnAction(this::handleViewComplaint);
        submitNewComplaintButton.setOnAction(this::handleSubmitNewComplaint);
    }

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        loadComplaints();
    }

    private void loadComplaints() {
        Customer customer = (Customer) client.getConnectedPerson();
        if (customer == null) {
            showAlert("Error", "No customer is connected.");
            return;
        }
        try {
            client.fetchCustomerComplaints();
        }catch(IOException e) {
            System.out.println("CustomerComplaintBoundary: cannot fetch the customer's complaint");
            e.printStackTrace();
        }
    }

    private void handleViewComplaint(ActionEvent event) {
        Complaint selectedComplaint = complaintTableView.getSelectionModel().getSelectedItem();
        if (selectedComplaint != null) {
            showComplaintDetails(selectedComplaint);
        } else {
            showAlert("Error", "Please select a complaint to view.");
        }
    }

    @Subscribe
    public void onCustomerComplaintListEvent(CustomerComplaintListEvent event) {
        System.out.println("DEBUG: in onComplaintListEvent");
        List<Complaint> complaints = event.getComplaints();
        if (complaints == null) {
            System.out.println("Complaints list is null. Initializing to an empty list.");
            complaints = new ArrayList<>(); // Initialize to an empty list if null
        }

        System.out.println("Loaded complaints: " + complaints.size() + " items.");
        complaintTableView.getItems().setAll(complaints);
    }



    void showComplaintDetails(Complaint complaint) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Complaint Details");
        dialog.setHeaderText(null);

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Title: " + complaint.getTitle()),
                new Label("Description: " + complaint.getDescription()),
                new Label("Date Submitted: " + complaint.getDate()),
                new Label("Status: " + (complaint.isActive() ? "Active" : "Resolved")),
                new Label("Cinema: " + (complaint.getCinemaName() != null ? complaint.getCinemaName() : "N/A")));

        if (!complaint.isActive()) {
            content.getChildren().addAll(
                    new Label("Response: " + complaint.getResponse()),
                    new Label("Refund Amount: $" + complaint.getRefund()));
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void requestCinemaList() {
        try {
            client.sendToServer(new Message(0, "getCinemaList"));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to request cinema list.");
        }
    }

    @FXML
    private void handleSubmitNewComplaint(ActionEvent event) {
        System.out.println("Submit New Complaint button pressed");

        Dialog<Complaint> dialog = new Dialog<>();
        dialog.setTitle("Submit New Complaint");
        dialog.setHeaderText(null);

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");

        cinemaComboBox.setPromptText("Select Cinema (Optional)");

        VBox content = new VBox(10, new Label("Title:"), titleField, new Label("Description:"), descriptionArea,
                new Label("Cinema:"), cinemaComboBox);
        dialog.getDialogPane().setContent(content);

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
        submitButton.addEventFilter(ActionEvent.ACTION, e -> {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (title.isEmpty() || description.isEmpty()) {
                e.consume();
                showAlert("Error", "Title and Description must not be empty.");
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                Customer customer = (Customer) client.getConnectedPerson();
                String selectedCinema = cinemaComboBox.getValue();
                if ("No Cinema".equals(selectedCinema)) {
                    selectedCinema = null; // Set to null if "No Cinema" is selected
                }
                return new Complaint(new java.util.Date(), titleField.getText().trim(),
                        descriptionArea.getText().trim(), true, customer, selectedCinema);
            }
            return null;
        });
        requestCinemaList();
        System.out.println("Cinema list requested");
        dialog.showAndWait().ifPresent(complaint -> {
            if (complaint != null) {
                try {
                    System.out.println("Submitting complaint");
                    client.submitComplaint(complaint);
                } catch (IOException e) {
                    System.out.println("Error submitting complaint: " + e.getMessage());
                    showAlert("Error", "Failed to submit complaint.");
                    e.printStackTrace();
                }
            }
        });

    }

    @Subscribe
    public void onCinemaListEvent(CinemaListEvent event) {
        System.out.println("Received cinema list event");
        Platform.runLater(() -> {
            cinemaComboBox.getItems().clear();
            cinemaComboBox.getItems().add("No Cinema");// Add "No Cinema" option
            ArrayList<String> cinemaNames = new ArrayList<>();
            for(Cinema c: event.getCinemas())
                cinemaNames.add(c.getCinemaName());
            cinemaComboBox.getItems().addAll(cinemaNames);
            System.out.println("Added cinemas to combo box: " + cinemaComboBox.getItems());
        });
    }

    @Subscribe
    public void onSubmitComplaintEvent(SubmitComplaintEvent event) {
        Platform.runLater(() -> {
            showAlert("Success", "The Complaint " + event.getComplaint().getTitle() + " was submitted successfully!");
        });
        try {
            client.fetchCustomerComplaints();
        } catch (IOException e) {
            showAlert("Error", "Failed to fetch customer complaints.");
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onRespondToComplaintEvent(RespondToComplaintEvent event) {
        Complaint c = event.getComplaint();
        if(c.getCustomer().getPersonId() == client.getConnectedPerson().getPersonId())
            Platform.runLater(() -> {
                showAlert("Got a response!", "A response to one of your complaint was just received, refreshing your list!");
                try {
                    client.fetchCustomerComplaints();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

    @FXML
    void handleBackButton(ActionEvent event) {
        try {
            cleanup();
            App.setRoot("CustomerMenu", null);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to the main menu.");
        }
    }
}