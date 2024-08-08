package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LinkDetailsBoundary implements DataInitializable {

    @FXML private Label movieTitleLabel;
    @FXML private Label watchLinkLabel;
    @FXML private Label validityPeriodLabel;
    @FXML private Button backHomeButton;

    private SimpleClient client;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        if (data instanceof String) {
            try {
                JsonNode bookingNode = objectMapper.readTree((String) data);
                movieTitleLabel.setText("Movie: " + bookingNode.get("movie").asText());
                watchLinkLabel.setText("Watch Link: " + bookingNode.get("watchLink").asText());
                validityPeriodLabel.setText("Valid from " + 
                    formatDate(bookingNode.get("openTime").asLong()) + 
                    " to " + formatDate(bookingNode.get("closeTime").asLong()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String formatDate(long timestamp) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));
    }

    @FXML
    private void handleBackHome() {
        try {
            App.setRoot("HomeMovieList");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}