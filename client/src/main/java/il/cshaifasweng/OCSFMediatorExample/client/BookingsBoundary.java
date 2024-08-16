package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.events.BookingListEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.CancelBookingEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public class BookingsBoundary implements DataInitializable {

    @FXML
    private VBox bookingsContainer;

    @FXML
    private TableView<Booking> bookingTable;

    @FXML
    private TableColumn<Booking, Integer> bookingIdColumn;

    @FXML
    private TableColumn<Booking, String> purchaseTimeColumn;

    @FXML
    private TableColumn<Booking, Integer> productsCountColumn;

    @FXML
    private TableColumn<Booking, Double> totalAmountPaidColumn;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private Button filterButton;

    @FXML
    private Button applyRefundButton;

    @FXML
    private Button showBookingsButton;

    private SimpleClient client;
//    private Person connectedPerson;
    private List<Booking> allBookings;

    public void initialize() throws IOException {
        EventBus.getDefault().register(this);

        client = SimpleClient.getClient(); // Ensure client is not null
/*        try {
            client.fetchRandomPerson(); // Fetch a random person from the server
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        //showBookings();
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        purchaseTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPurchaseTime().toString()));
        productsCountColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getProducts().size()).asObject());
        totalAmountPaidColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(calculateTotalAmountPaid(cellData.getValue())));

        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());

        filterButton.setOnAction(event -> applyFilters());
        showBookingsButton.setOnAction(event -> {
            try {
                showBookings();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        applyRefundButton.setOnAction(event -> {
            Booking selectedBooking = bookingTable.getSelectionModel().getSelectedItem();
            if (selectedBooking != null) {
                try {
                    applyForRefund(selectedBooking);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

/*    @Subscribe
    public void onFetchRandomPersonEvent(FetchRandomPersonEvent event) {
        this.connectedPerson = event.getPerson();
        System.out.println("Random person fetched: " + connectedPerson);
    }*/

    private double calculateTotalAmountPaid(Booking booking) {
        return booking.getProducts().stream().mapToDouble(Product::getPrice).sum();
    }

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @FXML
    private void showBookings() throws IOException {
        System.out.println("In showBookings - BookingsBoundary");
        if (client.getConnectedPerson() != null) {
            client.fetchUserBookings();
        } else {
            System.out.println("Connected person is not set.");
        }
    }

    @Override
    public void initData(Object data) {
    }

    @Subscribe
    public void onBookingListEvent(BookingListEvent event) {
        System.out.println("In onBookingListEvent");
        allBookings = event.getBookings();
        applyFilters();
    }

    private void applyFilters() {
        System.out.println("Applying filters in BookingsBoundary");
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        List<Booking> filteredBookings = allBookings.stream()
                .filter(booking -> {
                    LocalDate bookingDate = booking.getPurchaseTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return (fromDate == null || !bookingDate.isBefore(fromDate)) &&
                            (toDate == null || !bookingDate.isAfter(toDate));
                })
                .collect(Collectors.toList());

        displayFilteredBookings(filteredBookings);
    }

    private void displayFilteredBookings(List<Booking> bookings) {
        System.out.println("In displayFilteredBooking - BookingsBoundary");
        Platform.runLater(() -> {
            bookingTable.getItems().clear();
            bookingTable.getItems().addAll(bookings);
        });
    }

    private void applyForRefund(Booking booking) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        double totalRefund = 0.0;
        if (booking.getProducts().isEmpty() == false && booking.getProducts().get(0) instanceof TicketTab) {
            showAlert("Unable to refund Booking", "TicketTab cannot be refunded.");
            return;
        }
        for (Product product : booking.getProducts()) {
            double refundAmount = 0.0;

            if (product instanceof Ticket) {
                Ticket ticket = (Ticket) product;
                LocalDateTime screeningTime = ticket.getScreening().getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                if (screeningTime.isAfter(now.plusHours(3))) {
                    refundAmount = ticket.getPrice(); // Full refund
                } else if (screeningTime.isAfter(now.plusHours(1))) {
                    refundAmount = ticket.getPrice() * 0.5; // 50% refund
                } // No refund if within the last hour

            } else if (product instanceof HomeMovieLink) {
                HomeMovieLink homeMovieLink = (HomeMovieLink) product;
                LocalDateTime activationTime = homeMovieLink.getOpenTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                if (activationTime.isAfter(now.plusHours(1))) {
                    refundAmount = homeMovieLink.getPrice() * 0.5; // 50% refund
                } // No refund if within the last hour
            }
            totalRefund += refundAmount;
        }

        System.out.println("Total refund for booking " + booking.getBookingId() + ": " + totalRefund);
        // Send a message to the user
        client.cancelPurchase(booking.getBookingId(), totalRefund);
        // Implement the logic to process the refund, e.g., client.processRefund(booking.getBookingId(), totalRefund);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Subscribe
    public void onCancelBookingResponse(CancelBookingEvent event) {
        double refund = event.getRefund();
        Platform.runLater(() -> {
            showAlert("Booking Cancelled", "Your booking has been cancelled. you will be refunded " + refund);
            try {
                showBookings();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleBackButton(ActionEvent event) throws IOException {
        App.setRoot("CustomerMenu", null);
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}
