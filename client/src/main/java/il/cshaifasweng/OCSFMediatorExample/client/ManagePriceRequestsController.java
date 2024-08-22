package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import il.cshaifasweng.OCSFMediatorExample.client.events.MessageEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.PriceChangeRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.PriceChangeRequestsListEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import il.cshaifasweng.OCSFMediatorExample.entities.PriceChangeRequest;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ManagePriceRequestsController implements DataInitializable {
    @FXML
    private TableView<PriceChangeRequest> requestsTable;

    @FXML
    private TableColumn<PriceChangeRequest, String> movieColumn;

    @FXML
    private TableColumn<PriceChangeRequest, String> typeColumn;

    @FXML
    private TableColumn<PriceChangeRequest, Integer> oldPriceColumn;

    @FXML
    private TableColumn<PriceChangeRequest, Integer> newPriceColumn;

    @FXML
    private TableColumn<PriceChangeRequest, Date> requestDateColumn;

    @FXML
    private TableColumn<PriceChangeRequest, String> statusColumn;

    @FXML
    public void initialize() {
        objectMapper = new ObjectMapper();
        EventBus.getDefault().register(this);
        movieColumn.setPrefWidth(150);
        typeColumn.setPrefWidth(120);
        oldPriceColumn.setPrefWidth(80);
        newPriceColumn.setPrefWidth(80);
        requestDateColumn.setPrefWidth(200);
        statusColumn.setPrefWidth(100);
        movieColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMovie().getEnglishName()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMovieType()));
        oldPriceColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getOldPrice()).asObject());
        newPriceColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getNewPrice()).asObject());
        requestDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getRequestDate()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
    }

    private SimpleClient client;
    private ObjectMapper objectMapper;


    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        if (this.client != null) {
            loadPriceChangeRequests();
        }
    }

    private void loadPriceChangeRequests() {
        if (this.client != null) {
            try {
                client.sendToServer(new Message(0, "getPriceChangeRequests"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void approveRequest() {
        PriceChangeRequest selectedRequest = requestsTable.getSelectionModel().getSelectedItem();
        if (selectedRequest != null) {
            if (selectedRequest.getStatus().equals("Approved")) {
                showAlert("Cannot Approve", "This request has already been approved.");
            }else {
                try {
                    client.sendToServer(new Message(0, "approvePriceChangeRequest", String.valueOf(selectedRequest.getId())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    private void denyRequest() {
        PriceChangeRequest selectedRequest = requestsTable.getSelectionModel().getSelectedItem();
        if (selectedRequest != null) {
            if (selectedRequest.getStatus().equals("Approved")) {
                showAlert("Cannot Deny", "This request has already been approved and cannot be denied.");
            } else if(selectedRequest.getStatus().equals("Denied")) {
                showAlert("Cannot Deny", "This request has already been denied.");
            }else {
                try {
                    client.sendToServer(new Message(0, "denyPriceChangeRequest", String.valueOf(selectedRequest.getId())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    @Subscribe
//    public void onMessageEvent(MessageEvent event) {
//        Message message = event.getMessage();
//        Platform.runLater(() -> {
//            try {
//                if (message.getMessage().equals("priceChangeRequests")) {
//                    String jsonString = message.getData();
//                    List<PriceChangeRequest> requests = objectMapper.readValue(jsonString,
//                            new TypeReference<List<PriceChangeRequest>>() {});
//                    requestsTable.getItems().setAll(requests);
//                } else if (message.getMessage().contains("Price change request approved") ||
//                        message.getMessage().contains("Price change request denied")) {
//                    PriceChangeRequest updatedRequest = objectMapper.readValue(message.getData(), PriceChangeRequest.class);
//                    updateTableRow(updatedRequest);
//                } else if (message.getMessage().contains("Failed to approve") ||
//                        message.getMessage().contains("Failed to deny")) {
//                    showAlert("Error", message.getMessage());
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }

    @Subscribe
    public void onPriceChangeRequestsListEvent(PriceChangeRequestsListEvent event) {
        if(event.isSuccess())
        {
            List<PriceChangeRequest> requests = event.getRequests();
            Platform.runLater(() -> {
                requestsTable.getItems().setAll(requests);
            });
        }
    }



    @Subscribe
    public void onPriceChangeRequestEvent(PriceChangeRequestEvent event) {
        PriceChangeRequest updatedRequest = event.getRequest();
        PriceChangeRequest selectedRequest = requestsTable.getSelectionModel().getSelectedItem();
        boolean sameRequest = (selectedRequest != null && updatedRequest!=null && selectedRequest.getRequestDate().equals(updatedRequest.getRequestDate()));
        if(event.getMessage().equals("Approved") && sameRequest)
        {
            if(event.isSuccess())
            {
                client.showSuccessAlert("Price change request approved and price updated successfully");
                updateTableRow(updatedRequest);
            }else {
                client.showErrorAlert("Failed to approve the price change request");
            }

        } else if (event.getMessage().equals("Denied") && sameRequest) {
            if(event.isSuccess())
            {
                client.showSuccessAlert("Price change request denied successfully");
                updateTableRow(updatedRequest);
            }else{
                client.showErrorAlert("Failed to deny the request! It may already been approved.");
            }
        } else if (event.getMessage().equals("Created") && event.isSuccess()) {
            client.showAlert("A new price change request submitted!", "Updating the list!");
            Platform.runLater(() -> {
                try{
                    client.sendToServer(new Message(0, "getPriceChangeRequests"));
                } catch (IOException e) {
                    System.out.println("ManagePriceRequestsController: onPriceChangeRequestEvent: failed asking for" +
                            "the price change requests");
                    e.printStackTrace();
                }
            });
        }
    }




    private void updateTableRow(PriceChangeRequest updatedRequest) {
        for (int i = 0; i < requestsTable.getItems().size(); i++) {
            PriceChangeRequest request = requestsTable.getItems().get(i);
            if (request.getId() == updatedRequest.getId()) {
                requestsTable.getItems().set(i, updatedRequest);
                requestsTable.refresh();
                break;
            }
        }
    }

    @FXML
    private void goBack() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
        App.setRoot("WorkerMenu", connectedPerson);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }


}