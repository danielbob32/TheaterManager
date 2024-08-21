package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import il.cshaifasweng.OCSFMediatorExample.client.events.*;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleClient extends AbstractClient {

    private static SimpleClient client = null;
    private ObjectMapper objectMapper;
    private Person connectedPerson;

    private SimpleClient(String host, int port) {
        super(host, port);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.connectedPerson = null;
    }

    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof Message) {
            Message message = (Message) msg;
            System.out.println("in SimpleClient got " + message.getMessage());

            String[] messageParts = message.getMessage().split(":", 2);
            String messageType = messageParts[0];
            String messageStatus = messageParts.length > 1 ? messageParts[1] : "";

            switch (messageType) {
                case "movieList":
                    handleMovieList(message);
                    break;
                case "Customer login":
                    handleCustomerLogin(message, messageStatus);
                    break;
                case "Person login":
                    handlePersonLoginFail();
                    break;
                case "Price":
                    handlePriceUpdate(message, messageStatus);
                    break;
                case "Worker login":
                    handleWorkerLogin(message, messageStatus);
                    break;
                case "Cinema manager login":
                    handleCinemaManagerLogin(message, messageStatus);
                    break;
                case "Screening add":
                    handleScreeningAdd(message, messageStatus);
                    break;
                case "Screening delete":
                    handleScreeningDelete(message, messageStatus);
                    break;
                case "Movie delete":
                    handleMovieDelete(message, messageStatus);
                    break;
                case "Movie add":
                    handleMovieAdd(message, messageStatus);
                    break;
                case "Movie update":
                    handleMovieUpdate(message, messageStatus);
                    break;
                case "Price change request created":
                    showSuccessAlert("Price change request created successfully");
                    break;
                case "priceChangeRequests":
                    handlePriceChangeRequests(message);
                    break;
                case "Price change request approved and price updated successfully":
                    handlePriceChangeApproved(message);
                    break;
                case "Price change request denied":
                    handlePriceChangeDenied(message);
                    break;
                case "Price change request error":
                    showErrorAlert(message.getData(), "An error occurred with the price change request");
                    break;
                case "seatAvailabilityResponse":
                    handleSeatAvailabilityResponse(message);
                    break;
                case "ticketTabResponse":
                    handleTicketTabResponse(message);
                    break;
                case "addedTicketsSuccessfully":
                    handleAddedTicketsSuccessfully(message.getData());
                    break;
                case "addingTicketsFailed":
                    handleAddingTicketsFailed();
                    break;
                case "purchasedTicketTabSuccessfully":
                    handlePurchasedTicketTabSuccessfully(message);
                    break;
                case "purchasedHomeMovieLinkSuccessfully":
                    handlePurchasedHomeMovieLink(message, true);
                    break;
                case "purchasingHomeMovieLinkFailed":
                    handlePurchasedHomeMovieLink(message, false);
                    break;
                case "cinemaList":
                    handleCinemaList(message.getData());
                    break;
                case "reportData":
                    System.out.println("Received report data: " + message.getData());
                    handleReportData(message.getData());
                    break;
                case "fetchUserTicketTabsResponse":
                    handleFetchUserTicketTabsResponse(message);
                    break;
                // YONI`S CASES:
                case "fetchUserBookingsResponse":
                    handleFetchUserBookingsResponse(message);
                    break;
                case "cancelBookingResponse":
                    handleCancelBookingResponse(message);
                    break;
                case "submitComplaintResponse":
                    handleSubmitComplaintResponse(message);
                    break;
                case "fetchComplaintsResponse":
                    handleFetchComplaintsResponse(message);
                    break;
                case "fetchCustomerComplaintsResponse":
                    handleFetchCustomerComplaintsResponse(message);
                    break;
                case "respondToComplaintResponse":
                    handleRespondToComplaintResponse(message);
                    break;
                case "fetchRandomPersonResponse":
                    handleFetchRandomPersonResponse(message);
                    break;

                case "notifications":
                    try {
                        List<Notification> notifications = objectMapper.readValue(message.getData(),
                                new TypeReference<List<Notification>>() {
                                });
                        EventBus.getDefault().post(new NotificationEvent(notifications));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    System.out.println("Received unknown message in case notifications: " + message.getMessage());
                    break;
            }
        } else if (msg instanceof Warning) {
            Warning warning = (Warning) msg;
            EventBus.getDefault().post(new WarningEvent(warning));
        } else {
            System.out.println("Received unknown message type: " + msg.getClass().getName());
        }
    }

    private void showSuccessAlert(String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void showErrorAlert(String content) {
        showErrorAlert(content, null);
    }

    private void showErrorAlert(String content, String defaultContent) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(content != null ? content : defaultContent);
            alert.showAndWait();
        });
    }

    private void showFailAlert(String content) {
        showFailAlert(content, null);
    }

    private void showFailAlert(String content, String defaultContent) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fail");
            alert.setHeaderText(null);
            alert.setContentText(content != null ? content : defaultContent);
            alert.showAndWait();
        });
    }

    // ----- FUNCTION TO HANDLE THE RESPONSES FROM THE SERVER --------
    private void handleMovieList(Message message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message.getData());
            if (rootNode.isArray() && rootNode.size() > 0) {
                JsonNode firstMovieNode = rootNode.get(0);
                JsonNode genresNode = firstMovieNode.get("genres");
            }
            List<Movie> movies = objectMapper.readValue(message.getData(), new TypeReference<List<Movie>>() {
            });
            String movieType = message.getAdditionalData();
            if (movieType != null) {
                List<Movie> filteredMovies = movies.stream()
                        .filter(movie -> ("Cinema Movies".equals(movieType) && movie.getIsCinema()) ||
                                ("Home Movies".equals(movieType) && movie.getIsHome()))
                        .collect(Collectors.toList());
                EventBus.getDefault().post(new MovieListEvent(filteredMovies));
            } else {
                EventBus.getDefault().post(new MovieListEvent(movies));
            }
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to deserialize movies"));
        }

    }

    private void handleCustomerLogin(Message message, String status) {
        if (status.equals("successful")) {
            Platform.runLater(() -> {
                try {
                    Customer current = objectMapper.readValue(message.getData(), Customer.class);
                    login(current);
                    App.setRoot("CustomerMenu", current);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            showErrorAlert("ID was not found. Please check credentials and try again");
        }
    }

    public void handlePersonLoginFail() {
        showErrorAlert("This user is already logged in. Log out from the other device first.");
    }

    private void handlePriceUpdate(Message message, String status) {
        System.out.println("Price message received");
        if (status.equals("success")) {
            System.out.println("Prices updated successfully in simple client");
            showSuccessAlert("Price updated successfully");
            try {
                sendToServer(new Message(0, "getMovies", "Cinema Movies"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showErrorAlert("Failed to update prices");
        }
    }

    private void handleWorkerLogin(Message message, String status) {
        if (status.equals("successful")) {
            System.out.println("Worker login successful");
            Platform.runLater(() -> {
                try {
                    Worker current = objectMapper.readValue(message.getData(), Worker.class);
                    System.out.println("Deserialized object type: " + current.getClass().getName());

                    if (current instanceof CinemaManager) {
                        CinemaManager cinemaManager = (CinemaManager) current;
                        System.out.println("Cinema Manager detected: " + cinemaManager.getName());
                        login(cinemaManager);
                        App.setRoot("WorkerMenu", cinemaManager);
                    } else {
                        System.out.println("Regular Worker detected: " + current.getName());
                        login(current);
                        App.setRoot("WorkerMenu", current);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            showErrorAlert("One of the fields is incorrect. Please check credentials and try again");
        }
    }

    private void handleCinemaManagerLogin(Message message, String status) {
        if (status.equals("successful")) {
            System.out.println("Cinema Manager login successful");
            Platform.runLater(() -> {
                try {
                    CinemaManager current = objectMapper.readValue(message.getData(), CinemaManager.class);
                    login(current);
                    App.setRoot("WorkerMenu", current);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            showErrorAlert("One of the fields is incorrect. Please check credentials and try again");
        }

    }

    private void handleScreeningAdd(Message message, String status) {
        System.out.println("screening add message received");
        if (status.equals("success")) {
            System.out.println("Screening added successfully in simple client");
            showSuccessAlert("Screening added successfully");
            EventBus.getDefault().post(new MessageEvent(message));
        } else {
            showErrorAlert(message.getData(), "Failed to add screening");
        }
    }

    private void handleScreeningDelete(Message message, String status) {
        System.out.println("screening delete message received");
        if (status.equals("success")) {
            System.out.println("Screening deleted successfully in simple client");
            showSuccessAlert("Screening deleted successfully");
            EventBus.getDefault().post(new MessageEvent(message));
        } else {
            showErrorAlert("Failed to delete screening");
        }
    }

    private void handleMovieDelete(Message message, String status) {
        System.out.println("all movie delete message received");
        if (status.equals("success")) {
            showSuccessAlert("Movie deleted successfully");
        } else {
            showFailAlert("Movie is currently in use and cannot be deleted");
        }
    }

    private void handleMovieUpdate(Message message, String messageStatus) {
        if (messageStatus.equals("success")) {
            EventBus.getDefault().post(new MovieUpdateEvent(true));
        } else {
            EventBus.getDefault().post(new MovieUpdateEvent(false));
        }
    }

    private void handleMovieAdd(Message message, String status) {
        System.out.println("add movie message received");
        if (status.equals("success")) {
            showSuccessAlert("Movie have been added successfully");
        } else if (message.getData().equals("Movie already exists")) {
            showErrorAlert("This movie already exists in the database.");
        } else {
            showErrorAlert("Failed to add movie");
        }
    }

    private void handlePriceChangeRequests(Message message) {
        EventBus.getDefault().post(new MessageEvent(new Message(0, "priceChangeRequests", message.getData())));
    }

    private void handlePriceChangeApproved(Message message) {
        showSuccessAlert("Price change request approved and price updated successfully");
        EventBus.getDefault().post(new MessageEvent(new Message(0, message.getMessage(), message.getData())));
    }

    private void handlePriceChangeDenied(Message message) {
        showSuccessAlert("Price change request denied");
        EventBus.getDefault().post(new MessageEvent(new Message(0, message.getMessage(), message.getData())));
    }

    private void handleSeatAvailabilityResponse(Message message) {
        Platform.runLater(() -> {
            try {
                System.out.println("got seatAvailabilityResponse");
                List<Seat> seats = objectMapper.readValue(message.getData(), new TypeReference<List<Seat>>() {
                });
                EventBus.getDefault()
                        .post(new SeatAvailabilityEvent(Integer.parseInt(message.getAdditionalData()), seats));
            } catch (IOException e) {
                e.printStackTrace();
                EventBus.getDefault().post(new FailureEvent("Failed to deserialize seats"));
            }
        });
    }

    private void handleTicketTabResponse(Message message) {
        Platform.runLater(() -> {
            boolean isValid = Boolean.parseBoolean(message.getData());
            String ticketTabNumber = message.getAdditionalData();
            System.out.println("Received ticket tab response. Valid: " + isValid + ", Number: " + ticketTabNumber);
            EventBus.getDefault().post(new TicketTabResponseEvent(isValid, ticketTabNumber));
        });
    }

    private void handlePurchasedTicketTabSuccessfully(Message message) {
        Platform.runLater(() -> {
            System.out.println("got purchasedTicketTabSuccessfully");
            try {
                App.setRoot("TicketTabDetails", message.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handlePurchasedHomeMovieLink(Message message, boolean isSuccessful) {
        Platform.runLater(() -> {
            if (isSuccessful) {
                System.out.println("Received successful purchase response: " + message.getData());
                EventBus.getDefault()
                        .post(new HomeLinkPurchaseResponseEvent(true, "Purchase successful", message.getData()));
            } else {
                EventBus.getDefault().post(new HomeLinkPurchaseResponseEvent(false, "Purchase failed", null));
            }
        });
    }

    private void handleFetchUserBookingsResponse(Message message) {
        try {
            System.out.println("got fetchUserBookingsResponse");
            System.out.println("Received Data: " + message.getData());
            List<Booking> bookings = objectMapper.readValue(message.getData(), new TypeReference<List<Booking>>() {
            });
            System.out.println("Finished deserializing bookings - SimpleClient");
            for (Booking booking : bookings) {
                System.out.println("Booking ID: " + booking.getBookingId() + " - isActive: " + booking.isActive());
            }
            EventBus.getDefault().post(new BookingListEvent(Integer.parseInt(message.getAdditionalData()), bookings));
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to deserialize bookings"));
        }
    }

    private void handleFetchUserTicketTabsResponse(Message message) {
        try {
            System.out.println("got handleFetchUserTicketTabsResponse");
            System.out.println("Received Data: " + message.getData());
            List<TicketTab> ticketTabs = objectMapper.readValue(message.getData(),
                    new TypeReference<List<TicketTab>>() {
                    });
            System.out.println("Deserialized Data: " + ticketTabs);

            System.out.println("Finished deserializing ticket tabs");
            for (TicketTab ticketTab : ticketTabs) {
                System.out.println(
                        "TicketTab Product ID: " + ticketTab.getProduct_id() + " - isActive: " + ticketTab.isActive());
            }
            EventBus.getDefault()
                    .post(new TicketTabListEvent(Integer.parseInt(message.getAdditionalData()), ticketTabs));
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to deserialize ticket tabs"));
        }
    }

    private void handleCancelBookingResponse(Message message) {
        String[] parts = message.getAdditionalData().split(":");
        int bookingId = Integer.parseInt(parts[0]);
        double refund = Double.parseDouble(parts[1]);
        EventBus.getDefault().post(new CancelBookingEvent(bookingId, refund));
    }

    private void handleSubmitComplaintResponse(Message message) {
        EventBus.getDefault().post(new SubmitComplaintEvent(message.getAdditionalData()));
    }

    private void handleFetchComplaintsResponse(Message message) {
        try {
            List<Complaint> complaints = objectMapper.readValue(message.getData(),
                    new TypeReference<List<Complaint>>() {
                    });
            System.out.println("Got all the complaints, size: " + complaints.size());
            EventBus.getDefault().post(new ComplaintListEvent(complaints));
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to deserialize complaints"));
        }
    }

    private void handleFetchCustomerComplaintsResponse(Message message) {
        try {
            List<Complaint> complaints = objectMapper.readValue(message.getData(),
                    new TypeReference<List<Complaint>>() {
                    });
            System.out.println("Got customer complaints, size: " + complaints.size());
            EventBus.getDefault().post(new CustomerComplaintListEvent(complaints));
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to deserialize complaints"));
        }
    }

    private void handleRespondToComplaintResponse(Message message) {
        EventBus.getDefault().post(new RespondToComplaintEvent(message.getAdditionalData()));
    }

    private void handleFetchRandomPersonResponse(Message message) {
        try {
            Person person = objectMapper.readValue(message.getData(), Person.class);
            EventBus.getDefault().post(new FetchRandomPersonEvent(person));
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to deserialize person"));
        }
    }

    public static SimpleClient getClient() {
        if (client == null) {
            client = new SimpleClient("localhost", 3000);
            // client = new SimpleClient("0.tcp.eu.ngrok.io", 16312);
        }
        return client;
    }

    public static void setClient(SimpleClient newClient) {
        client = newClient;
    }

    public void tryWorkerLogin(Worker worker) {
        try {
            sendToServer(new Message(0, "login:worker", serializeWorker(worker)));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to update movie show times");
        }
    }

    public void tryCustomerLogin(Customer customer) {
        try {
            sendToServer(new Message(0, "login:customer", objectMapper.writeValueAsString(customer)));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to recognize this customer");
        }
    }

    public String serializeWorker(Worker worker) {
        try {
            return objectMapper.writeValueAsString(worker);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("couldn't serialize worker");
            return null;
        }
    }

    public void sendToServer(Object msg) throws IOException {
        System.out.println("Sending to server: " + msg);
        super.sendToServer(msg);
    }

    public void openConnection() throws IOException {
        System.out.println("Opening connection to server");
        super.openConnection();
    }

    public void getMovies() {
        try {
            System.out.println("Sending getMovies request to server");
            sendToServer(new Message(0, "getMovies"));
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to request movies"));
        }
    }

    public void getMovieById(int id) {
        try {
            Message message = new Message(0, "getMovieById", "", id);
            sendToServer(message);
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to request movie"));
        }
    }

    public void getSeatAvailability(int screeningId) throws IOException {
        Message message = new Message(0, "getSeatAvailability", String.valueOf(screeningId));
        sendToServer(message);
    }

    public void purchaseTickets(String name, String id, String email, String paymentMethod, String paymentNum,
            int cinemaPrice, int screeningId, List<Integer> chosenSeatsId) throws IOException {
        System.out.println("in SimpleClient: purchaseTickets for " + name);
        try {
            ObjectNode dataNode = objectMapper.createObjectNode();
            dataNode.put("name", name);
            dataNode.put("id", id);
            dataNode.put("email", email);
            dataNode.put("paymentMethod", paymentMethod);
            dataNode.put("paymentNum", paymentNum);
            dataNode.put("cinemaPrice", cinemaPrice);
            dataNode.put("screeningId", screeningId);

            ObjectNode seatIdNode = objectMapper.createObjectNode();
            for (int seatId : chosenSeatsId) {
                seatIdNode.put(String.valueOf(seatId), true);
            }
            dataNode.set("seatIds", seatIdNode);

            String jsonData = objectMapper.writeValueAsString(dataNode);

            sendToServer(new Message(0, "processPayment", jsonData));

        } catch (Exception e) {
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to process payment data"));
        }
    }

    public void checkTicketTabValidity(int ticketTabId, int customerId, int seatsNum) throws IOException {
        Message message = new Message(0, "checkTicketTab", ticketTabId + "," + customerId + "," + seatsNum);
        sendToServer(message);
    }

    private void handleAddedTicketsSuccessfully(String bookingDataJson) {
        try {
            EventBus.getDefault().post(new PurchaseResponseEvent(true, "purchase successful", bookingDataJson));
        } catch (Exception e) {
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to process payment data"));
        }
    }

    private void handleAddingTicketsFailed() {
        try {
            EventBus.getDefault().post(new PurchaseResponseEvent(false, "purchase failed"));
        } catch (Exception e) {
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to process payment data"));
        }
    }

    public void purchaseTicketTab(String id, String name, String email, String cardNum) {
        System.out.println("in SimpleClient: addTicketTab");
        try {
            ObjectNode dataNode = objectMapper.createObjectNode();
            dataNode.put("id", id);
            dataNode.put("name", name);
            dataNode.put("email", email);
            dataNode.put("creditCard", cardNum);

            String jsonData = objectMapper.writeValueAsString(dataNode);
            sendToServer(new Message(0, "purchaseTicketTab", jsonData));

        } catch (Exception e) {
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to purchase ticket tab"));
        }
    }

    public void purchaseLink(int movieId, String selectedDate, String selectedTime, int totalPrice, String email,
            String name, String id, String creditCard) {
        try {
            ObjectNode dataNode = objectMapper.createObjectNode();
            dataNode.put("movieId", movieId);
            dataNode.put("selectedDate", selectedDate);
            dataNode.put("selectedTime", selectedTime);
            dataNode.put("totalPrice", totalPrice);
            dataNode.put("email", email);
            dataNode.put("name", name);
            dataNode.put("id", id);
            dataNode.put("creditCard", creditCard);

            String jsonData = objectMapper.writeValueAsString(dataNode);

            sendToServer(new Message(0, "purchaseLink", jsonData));
        } catch (Exception e) {
            System.out.println("DEBUG: Error in purchaseLink - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleCinemaList(String data) {
        System.out.println("Received cinema list data: " + data);
        try {
            List<String> cinemas = objectMapper.readValue(data, new TypeReference<List<String>>() {
            });
            System.out.println("Parsed cinema list: " + cinemas);
            EventBus.getDefault().post(new CinemaListEvent(cinemas));
        } catch (IOException e) {
            System.out.println("Error parsing cinema list: " + e.getMessage());
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to parse cinema list"));
        }
    }

    private void handleReportData(String data) {
        try {
            System.out.println("Processing report data: " + data);
            JsonNode reportNode = objectMapper.readTree(data);
            String reportType = reportNode.get("reportType").asText();
            String reportData = reportNode.get("reportData").asText();
            System.out.println("Posting report event: type=" + reportType + ", data=" + reportData);
            EventBus.getDefault().post(new ReportDataEvent(reportType, reportData));
        } catch (IOException e) {
            System.out.println("Error parsing report data: " + e.getMessage());
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to parse report data"));
        }
    }

    public void requestCinemaList() throws IOException {
        System.out.println("Sending getCinemaList request to server");
        sendToServer(new Message(0, "getCinemaList"));
    }

    public void requestReport(String reportType, LocalDate month, String cinema) throws IOException {
        Person connectedPerson = getConnectedPerson();
        System.out.println("Connected person class inside simple client: " + connectedPerson.getClass().getName());
        System.out.println("Requesting report for connected person: " + connectedPerson.getName() + ", type: "
                + connectedPerson.getClass().getSimpleName());

        if (connectedPerson instanceof CinemaManager) {
            CinemaManager manager = (CinemaManager) connectedPerson;
            cinema = manager.getCinema().getCinemaName();
            reportType = "Monthly Ticket Sales Manager"; // Correct the report type for CinemaManager
            System.out.println("CinemaManager detected. Requesting report: " + reportType + " for cinema: " + cinema);
        } else {
            System.out.println("Connected person is not a CinemaManager");
        }

        ObjectNode dataNode = objectMapper.createObjectNode();
        dataNode.put("reportType", reportType);
        dataNode.put("month", month.toString());
        dataNode.put("cinema", cinema);

        String jsonData = objectMapper.writeValueAsString(dataNode);
        System.out.println("Sending report request: " + jsonData);
        sendToServer(new Message(0, "generateReport", jsonData));
    }

    public void updateMovie(Movie movie) {
        try {
            String jsonData = objectMapper.writeValueAsString(movie);
            Message updateMovieMsg = new Message(0, "updateMovie", jsonData);
            sendToServer(updateMovieMsg);
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new FailureEvent("Failed to update movie"));
        }
    }

    public void login(Person p) {
        setClient(this);
        this.connectedPerson = p;
        System.out.println("now connected: " + p.getName());
    }

    public void logout() {
        if (this.connectedPerson != null) {
            try {
                Message logout_message = new Message(0, "personLogout",
                        String.valueOf(this.connectedPerson.getPersonId()));
                sendToServer(logout_message);
            } catch (IOException e) {
                System.out.println("Error sending the server logout request");
                e.printStackTrace();
            }
        }

        this.connectedPerson = null;
    }

    public Person getConnectedPerson() {
        return connectedPerson;
    }

    public void fetchUserTicketTabs() throws IOException {
        Message message = new Message(0, "fetchUserTicketTabs", String.valueOf(this.connectedPerson.getPersonId()));
        sendToServer(message);
    }

    public void fetchUserBookings() throws IOException {
        Message message = new Message(0, "fetchUserBookings", String.valueOf(this.connectedPerson.getPersonId()));
        sendToServer(message);
    }

    public void cancelPurchase(int purchaseId, double refund) throws IOException {
        Message message = new Message(0, "cancelPurchase", String.valueOf(purchaseId), String.valueOf(refund));
        sendToServer(message);
    }

    public void submitComplaint(Complaint data) throws IOException {
        String jsonData = objectMapper.writeValueAsString(data);
        Message message = new Message(0, "submitComplaint", jsonData);
        sendToServer(message);
    }

    public void respondToComplaint(Complaint complaint) {
        try {
            int complaintId = complaint.getComplaint_id();
            String response = complaint.getResponse();
            int refund = complaint.getRefund();
            sendToServer(new Message(0, "respondToComplaint", complaintId + ";" + response + ";" + refund));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fetchAllComplaints() throws IOException {
        Message message = new Message(0, "fetchAllComplaints");
        sendToServer(message);
    }

    public void fetchCustomerComplaints() throws IOException {
        Message message = new Message(0, "fetchCustomerComplaints", String.valueOf(this.connectedPerson.getPersonId()));
        sendToServer(message);
    }

    public void fetchComplaints(String status) throws IOException {
        Message message = new Message(0, "fetchComplaints", status);
        sendToServer(message);
    }

    public void requestNotifications() throws IOException {
        int customerId = ((Customer) connectedPerson).getPersonId();
        sendToServer(new Message(0, "getNotifications", String.valueOf(customerId)));
    }

    public void markNotificationAsRead(int notificationId) throws IOException {
        int customerId = ((Customer) connectedPerson).getPersonId();
        sendToServer(new Message(0, "markNotificationAsRead", notificationId + "," + customerId));
    }

    public void markNotificationsAsRead(List<Integer> notificationIds) throws IOException {
        sendToServer(new Message(0, "markNotificationsAsRead", objectMapper.writeValueAsString(notificationIds)));
    }

}
