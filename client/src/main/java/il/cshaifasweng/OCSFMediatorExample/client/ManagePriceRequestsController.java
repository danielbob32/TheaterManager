package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import il.cshaifasweng.OCSFMediatorExample.client.events.MessageEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.PriceChangeRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;

public class ManagePriceRequestsController implements DataInitializable {
    @FXML
    private TableView<PriceChangeRequest> requestsTable;

    private SimpleClient client;
    private ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);
        loadPriceChangeRequests();
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        loadPriceChangeRequests();
    }

    private void loadPriceChangeRequests() {
        try {
            client.sendToServer(new Message(0, "getPriceChangeRequests"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void approveRequest() {
        PriceChangeRequest selectedRequest = requestsTable.getSelectionModel().getSelectedItem();
        if (selectedRequest != null) {
            try {
                client.sendToServer(new Message(0, "approvePriceChangeRequest", String.valueOf(selectedRequest.getId())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void denyRequest() {
        PriceChangeRequest selectedRequest = requestsTable.getSelectionModel().getSelectedItem();
        if (selectedRequest != null) {
            try {
                client.sendToServer(new Message(0, "denyPriceChangeRequest", String.valueOf(selectedRequest.getId())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe
    public void onMessageEvent(MessageEvent event) {
        Message message = event.getMessage();
        if (message.getMessage().equals("priceChangeRequests")) {
            Platform.runLater(() -> {
                try {
                    List<PriceChangeRequest> requests = objectMapper.readValue(message.getData(),
                            new TypeReference<List<PriceChangeRequest>>() {});
                    requestsTable.getItems().setAll(requests);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (message.getMessage().equals("Price change request approved") ||
                message.getMessage().equals("Price change request denied")) {
            loadPriceChangeRequests(); // Refresh the list after an approval or denial
        }
    }
}