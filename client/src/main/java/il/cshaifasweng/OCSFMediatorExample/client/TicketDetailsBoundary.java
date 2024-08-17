package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TicketDetailsBoundary implements DataInitializable {

    private SimpleClient client;

    @FXML
    private Text bookingIdText;

    @FXML
    private Text movieHallText;

    @FXML
    private Text movieText;

    @FXML
    private Text nameText;

    @FXML
    private Text purchaseTimeText;

    @FXML
    private Text screeningTimeText, ticketNumText;

    @FXML
    private Text seatsText, amountLeftText, amountLeftText1, cinemaText;

    @FXML
    private Button homePageButton;

    int bookingId, ticketNum, amountLeft;
    String name, seats, movie, cinema, movieHall, paymentMethod, screeningTime, purchaseTime;



    @Override
    public void initData(Object data) {
        if (data instanceof String) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode bookingData = objectMapper.readTree((String) data);

                // Use these values to update your UI or store them in class fields
                bookingId = bookingData.path("bookingId").asInt(-1);  // -1 as default if missing
                name = bookingData.path("name").asText("N/A");  // "N/A" as default if missing
                ticketNum = bookingData.path("ticketNum").asInt(0);  // 0 as default if missing
                seats = bookingData.path("seats").asText("");  // Empty string as default if missing
                movie  = bookingData.path("movie").asText("");
                cinema = bookingData.path("cinema").asText("");
                movieHall = bookingData.path("movieHall").asText("");
                paymentMethod = bookingData.path("paymentMethod").asText("");
                if (paymentMethod.equals("ticketTab")) {
                    amountLeft = bookingData.path("amountLeft").asInt(0);
                }

                System.out.println("in TicketDetailsBoundary initData with: " + name);

                long screeningTimeMillis = bookingData.path("screeningTime").asLong(0);
                long purchaseTimeMillis = bookingData.path("purchaseTime").asLong(0);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                // Process screening time
                if (screeningTimeMillis != 0) {
                    Date screeningTime = new Date(screeningTimeMillis);
                    String formattedScreeningTime = dateFormat.format(screeningTime);
                    screeningTimeText.setText(formattedScreeningTime);
                } else {
                    screeningTimeText.setText("Not available");
                }

                // Process purchase time
                if (purchaseTimeMillis != 0) {
                    Date purchaseTime = new Date(purchaseTimeMillis);
                    String formattedPurchaseTime = dateFormat.format(purchaseTime);
                    purchaseTimeText.setText(formattedPurchaseTime);
                } else {
                    purchaseTimeText.setText("Not available");
                }
                updateUI();

            } catch (JsonProcessingException e) {
                e.printStackTrace();
                // Handle JSON parsing error
                showAlert("Error parsing booking data");
            }
        } else {
            // Handle case where data is not a String
            showAlert("Invalid data format received");
        }
    }

    private void updateUI() {
        bookingIdText.setText(String.valueOf(bookingId));
        nameText.setText(name);
        ticketNumText.setText(String.valueOf(ticketNum));
        seatsText.setText(seats);
        movieText.setText(movie);
        movieHallText.setText(movieHall);
        cinemaText.setText(cinema);
        if (paymentMethod.equals("ticketTab")) {
            amountLeftText.setVisible(true);
            amountLeftText1.setVisible(true);
            amountLeftText.setText(String.valueOf(amountLeft));
        } else {
            amountLeftText.setVisible(false);
            amountLeftText1.setVisible(false);
        }
    }

    @FXML
    private void handleBackButton() throws IOException {
        App.setRoot("CinemaMovieList", null);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }
}
