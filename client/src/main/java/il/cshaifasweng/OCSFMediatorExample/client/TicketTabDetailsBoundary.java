package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TicketTabDetailsBoundary implements DataInitializable{

    private SimpleClient client;

    @FXML private AnchorPane orderDetailsPane;
    @FXML private Button homePageButton;

    @FXML
    private Text customerName, numOfTickets, ticketTabNum, purchaseTime, bookingIdText;

    private String name, bookingId, time, ticketTabId;

    @Override
    public void setClient(SimpleClient client) { this.client = client; }

    @Override
    public void initData(Object data) {
        if (data instanceof String) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode bookingData = objectMapper.readTree((String) data);
                name = bookingData.path("name").asText("");
                bookingId = bookingData.path("bookingId").asText("");
                time = bookingData.path("purchaseTime").asText("");
                ticketTabId = bookingData.path("ticketTabId").asText("");
                initialize();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                // Handle JSON parsing error
                showAlert("Error parsing booking data");
            }
        }

    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        customerName.setText(name);
        ticketTabNum.setText(ticketTabId);
        numOfTickets.setText("20");
        bookingIdText.setText(bookingId);
        //purchaseTime.setText(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        purchaseTime.setText(dateFormat.format(new Date()));

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date purchaseDate = new Date(time);
//        String formattedTime = sdf.format(purchaseDate);
//        purchaseTime.setText(formattedTime);
    }

    @FXML
    public void handleHomePageButton(javafx.event.ActionEvent actionEvent) throws IOException {
        Person connectedPerson = client.getConnectedPerson();

        if (connectedPerson != null) {
            App.setRoot("CustomerMenu", null);
        }
        else {
            App.setRoot("Loginpage", null);
        }
    }
}


