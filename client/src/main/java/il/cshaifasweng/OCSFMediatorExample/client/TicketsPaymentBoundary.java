package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.client.events.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TicketsPaymentBoundary implements DataInitializable {

    @FXML private Label movieTitleLabel;
    @FXML private Label cinemaLabel;
    @FXML private Label screeningTimeLabel;
    @FXML private Label selectedSeatsLabel;
    @FXML private Label totalPriceLabel;

    @FXML private RadioButton creditCardRadio;
    @FXML private RadioButton ticketTabRadio;
    @FXML private VBox creditCardForm;
    @FXML private VBox ticketTabForm;
    @FXML private ImageView movieImageView;

    @FXML private TextField nameField;
    @FXML private TextField idField;
    @FXML private TextField emailField;
    @FXML private TextField creditCardNumberField;
    @FXML private TextField ticketTabNumberField;

    @FXML private Button confirmPaymentButton;
    @FXML private Button backButton;

    private SimpleClient client;
    private TicketPurchaseInfo purchaseInfo;

    private Screening screening;
    private List<Seat> chosenSeats;
    private List<Integer> chosenSeatsId;

    private String name;
    private String id;
    private String email;
    private String paymentMethod;
    private String paymentNum;   

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        if (data instanceof TicketPurchaseInfo) {
            this.purchaseInfo = (TicketPurchaseInfo) data;
            initializePaymentDetails();
        }
    }

    @FXML
    private void initialize() {
        EventBus.getDefault().register(this);
        ToggleGroup paymentToggle = new ToggleGroup();
        creditCardRadio.setToggleGroup(paymentToggle);
        ticketTabRadio.setToggleGroup(paymentToggle);

        creditCardRadio.setSelected(true);
        updatePaymentForm();

        paymentToggle.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            updatePaymentForm();
        });

    }

    private void initializePaymentDetails() {
        screening = purchaseInfo.getScreening();
        chosenSeats = purchaseInfo.getSelectedSeats();
        chosenSeatsId = new ArrayList<>();
        for (Seat seat : chosenSeats) {
            chosenSeatsId.add(seat.getSeat_id());
        }

        movieTitleLabel.setText(screening.getMovie().getEnglishName());
        cinemaLabel.setText(screening.getCinema().getCinemaName());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        screeningTimeLabel.setText(dateFormat.format(screening.getTime()));

        StringBuilder seatsString = new StringBuilder();
        for (Seat seat : purchaseInfo.getSelectedSeats()) {
            seatsString.append(seat.getSeatRow()).append("-").append(seat.getSeatNumber()).append(", ");
        }
        selectedSeatsLabel.setText("Selected Seats: " + seatsString.substring(0, seatsString.length() - 2));

        totalPriceLabel.setText("Total Price: ₪" + purchaseInfo.getTotalPrice());

        Person connectedPerson = client.getConnectedPerson();
        if (connectedPerson instanceof Customer) {
            nameField.setText(connectedPerson.getName());
            idField.setText(String.valueOf(connectedPerson.getPersonId()));
            emailField.setText(((Customer) connectedPerson).getEmail());
        }

        byte[] image2 = screening.getMovie().getMovieIcon();
        Image image3 = convertByteArrayToImage(image2);
        if (image3 == null || image3.isError()) {
            System.out.println("Using default image");
            try {
                InputStream defaultImageStream = getClass().getClassLoader().getResourceAsStream("Images/default.jpg");
                if (defaultImageStream != null) {
                    image3 = new Image(defaultImageStream);
                    System.out.println("Default image loaded successfully");
                } else {
                    System.out.println("Default image not found");
                }
            } catch (Exception e) {
                System.out.println("Error loading default image: " + e.getMessage());
            }
        }
        movieImageView.setImage(image3);
    }

    public Image convertByteArrayToImage(byte[] imageData) {
        if (imageData != null && imageData.length > 0) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            return new Image(inputStream);
        } else {
            System.out.println("No image data available");
            return null; 
        }
    }

    private void updatePaymentForm() {
        creditCardForm.setVisible(creditCardRadio.isSelected());
        creditCardForm.setManaged(creditCardRadio.isSelected());
        ticketTabForm.setVisible(ticketTabRadio.isSelected());
        ticketTabForm.setManaged(ticketTabRadio.isSelected());
    }

    @FXML
    private void handleConfirmPayment() {
        if (!validateInput()) {
            return;
        }
        updatePurchaseInfoWithPaymentDetails();

        try {
            System.out.println("in Tickets payment boundary, sending name: " + name);
            client.purchaseTickets(name, id, email, paymentMethod, paymentNum,
                    screening.getMovie().getCinemaPrice(), screening.getScreening_id(), chosenSeatsId);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error processing payment. Please try again.");
        }
    }

    private boolean validateInput() {
        if (creditCardRadio.isSelected()) {
            return validateName() && validateId() && validateEmail() && validateCreditCard();
        } else {
            validateTicketTab();
            return false;
        }
    }

    private boolean validateName() {
        String name = nameField.getText().trim();
        if (!Pattern.matches("[a-zA-Z\\s]+", name)) {
            showAlert("Invalid name. Please use only letters and spaces.");
            return false;
        }
        return true;
    }

    private boolean validateId() {
       String id = idField.getText().trim();
       if (!Pattern.matches("\\d{9}", id) && !Pattern.matches("\\d{4}", id)) {
           showAlert("Invalid ID. Please enter 9 or 4 digits.");
           return false;
       }
        return true;
    }

    private boolean validateEmail() {
        String email = emailField.getText().trim();
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!Pattern.matches(emailRegex, email)) {
            showAlert("Invalid email format.");
            return false;
        }
        return true;
    }

    private boolean validateCreditCard() {
        String creditCard = creditCardNumberField.getText().trim();
        if (!Pattern.matches("\\d{10}", creditCard)) {
            showAlert("Invalid credit card number. Please enter 10 digits.");
            return false;
        }
        return true;
    }

    private boolean validateTicketTab() {
        paymentNum = ticketTabNumberField.getText().trim();
        id = idField.getText().trim();
        try {
            int ticketTabId = Integer.parseInt(paymentNum);
            int customerId = Integer.parseInt(id);
            client.checkTicketTabValidity(ticketTabId, customerId, chosenSeats.size());
            return true;
        } catch (NumberFormatException e) {
            showAlert("Invalid ticket tab ID or customer ID. Please enter valid numbers.");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error checking ticket tab validity. Please try again.");
            return false;
        }
    }

    private void updatePurchaseInfoWithPaymentDetails() {
        name = nameField.getText().trim();
        id = idField.getText().trim();
        email = emailField.getText().trim();

        System.out.println("updating info with name: " + name);

        if (creditCardRadio.isSelected()) {
            paymentMethod = "creditCard";
            paymentNum = creditCardNumberField.getText().trim();
        }
        else {
            paymentMethod = "ticketTab";
            paymentNum = ticketTabNumberField.getText().trim();
        }
    }

    @Subscribe
    public void onTicketTabResponse(TicketTabResponseEvent event) {
        System.out.println("Received TicketTabResponseEvent. Valid: " + event.isValid() + ", Number: " + event.getTicketTabNumber());
        Platform.runLater(() -> {
            if (!event.isValid()) {
                System.out.println("Ticket tab validation failed");
                showAlert("Invalid information or insufficient balance.");
            } else {
                updatePurchaseInfoWithPaymentDetails();
                try {
                    System.out.println("in Tickets payment boundary, sending name: " + name);
                    client.purchaseTickets(name, id, email, paymentMethod, paymentNum,
                            screening.getMovie().getCinemaPrice(), screening.getScreening_id(), chosenSeatsId);

                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error processing payment. Please try again.");
                }
            }
        });
    }

    @FXML
    private void handleBack() {
        try {
            cleanup();
            App.setRoot("PurchaseTickets", purchaseInfo.getScreening());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error returning to ticket selection.");
        }
    }

    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payment Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Subscribe
    public void onPaymentResponse(PurchaseResponseEvent event) {
        if(event.isSuccess())
        {
            try{
                String currentCustomerId = idField.getText().trim();
                String data = event.getData();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode bookingData = objectMapper.readTree(data);
                String buyerId = bookingData.path("customerId").asText("");
                if(currentCustomerId.equals(buyerId))
                    Platform.runLater(() -> {
                            showAlert("Payment successful! Your tickets have been booked.");
                            try {
                                cleanup();
                                App.setRoot("TicketDetails", event.getData());
                            } catch (IOException e) {
                                e.printStackTrace();
                                showAlert("Error navigating to order details.");
                            }
                    });
            }catch(Exception e) {
                System.out.println("TicketPaymentBoundary: Error reading the booking data");
                e.printStackTrace();
            }
        }
        else {
            String errorStatus = event.getMessage();
            try{
                switch(errorStatus)
                {
                    case "screening deleted" -> {
                        client.showErrorAlert("The screening was just deleted! \n Moving you back to the movie page!");
                        cleanup();
                        App.setRoot("CinemaMovieDetails", screening.getMovie());
                        return;
                    }
                    case "movie deleted" -> {
                        client.showErrorAlert("The movie was just deleted! \n Moving you back to the movies list!");
                        cleanup();
                        App.setRoot("CinemaMovieList", null);
                        return;
                    }
                    case "seat unavailable" -> {
                        client.showErrorAlert("Oops! \n The seats you're trying to buy were just caught \n " +
                                "Moving you back to the movie page!");
                        cleanup();
                        App.setRoot("CinemaMovieDetails", screening.getMovie());
                        return;
                    }
                }
                client.showErrorAlert("There was an error, try again later.");
                cleanup();
                App.setRoot("CinemaMovieDetails", null);
            }catch (IOException e) {
                e.printStackTrace();
                showAlert("Error navigating to order details.");
            }


        }

    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}