package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LinkDetailsBoundary implements DataInitializable {
    @FXML private Text movieTitleText;
    @FXML private Text watchLinkText;
    @FXML private Text validityPeriodText;
    @FXML private Button backHomeButton;
    @FXML private Text bookingIdText;
    @FXML private Text purchaseTimeText;

    private SimpleClient client;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        System.out.println("LinkDetailsBoundary initData called with: " + data);
        if (data instanceof String) {
            try {
                JsonNode bookingNode = objectMapper.readTree((String) data);
                updateUI(bookingNode);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error parsing booking data: " + e.getMessage());
            }
        } else {
            System.out.println("Unexpected data type: " + (data != null ? data.getClass().getName() : "null"));
        }
    }

    private void updateUI(JsonNode bookingNode) {
        movieTitleText.setText("Movie: " + bookingNode.path("movie").asText("N/A"));
        watchLinkText.setText("Watch Link: " + bookingNode.path("watchLink").asText("N/A"));
        validityPeriodText.setText("Valid from " + 
            formatDate(bookingNode.path("openTime").asLong(0)) + 
            " to " + formatDate(bookingNode.path("closeTime").asLong(0)));
        bookingIdText.setText("Order number: " + bookingNode.path("bookingId").asText("N/A"));
        purchaseTimeText.setText("Purchase time: " + formatDate(bookingNode.path("purchaseTime").asLong(0)));
    }

    private String formatDate(long timestamp) {
        return timestamp == 0 ? "N/A" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
    }

    @FXML
    private void handleBackHome() {
        try {
            cleanup();
            App.setRoot("HomeMovieList", null);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error navigating back to home: " + e.getMessage());
        }
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

}
