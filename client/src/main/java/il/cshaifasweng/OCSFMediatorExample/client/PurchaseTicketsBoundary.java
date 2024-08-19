package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.events.SeatAvailabilityEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Screening;
import il.cshaifasweng.OCSFMediatorExample.entities.Seat;
import il.cshaifasweng.OCSFMediatorExample.entities.TicketPurchaseInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PurchaseTicketsBoundary implements DataInitializable {

    @FXML private Label movieTitleLabel;
    @FXML private Label cinemaLabel;
    @FXML private Label screeningTimeLabel;
    @FXML private GridPane seatGrid;
    @FXML private ImageView movieImageView;
    @FXML private VBox leftPane;
    @FXML private VBox rightPane;
    @FXML private Label totalPriceLabel;
    @FXML private Button backButton;
    @FXML private Button purchaseButton;

    private SimpleClient client;
    private Screening screening;
    private int totalPrice;
    private List<Seat> selectedSeats = new ArrayList<>();

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        if (data instanceof Screening) {
            this.screening = (Screening) data;
            initializeScreeningDetails();
            requestSeatAvailability();
        }
    }

    private void initializeScreeningDetails() {
        movieTitleLabel.setText(screening.getMovie().getEnglishName());
        cinemaLabel.setText(screening.getCinema().getCinemaName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        screeningTimeLabel.setText(dateFormat.format(screening.getTime()));

        // initialize image
        byte[] image2 = screening.getMovie().getMovieIcon();
        if (image2 != null) {
            System.out.println("Image byte array length: " + image2.length);
        } else {
            System.out.println("Image byte array is null");
        }
        Image image3 = convertByteArrayToImage(image2);
        if (image3 == null) {
            System.out.println("Image is null");
        } else {
            System.out.println("Image created successfully");
        }
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
            // Convert byte[] to InputStream
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);

            // Create Image from InputStream
            return new Image(inputStream);
        } else {
            System.out.println("No image data available");
            return null;  // Or handle as needed, e.g., return a default image
        }
    }

    private void requestSeatAvailability() {
        try {
            client.getSeatAvailability(screening.getScreening_id());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error occurred while fetching seat availability. Please try again.");
        }
    }

    @Subscribe
    public void onSeatAvailabilityResponse(SeatAvailabilityEvent event) {
        if (event.getScreeningId() == screening.getScreening_id()) {
            Platform.runLater(() -> {
                screening.setSeats(event.getSeats());
                initializeSeatGrid();
            });
        }
    }

    private void initializeSeatGrid() {
        rightPane.getChildren().clear();

        StackPane screenPane = createScreenRectangle();
        rightPane.getChildren().add(screenPane);

        seatGrid = new GridPane();
        seatGrid.setAlignment(Pos.CENTER);
        seatGrid.setHgap(10);
        seatGrid.setVgap(10);

        int cols = 10;
        int rows = screening.getHall().getHallNumber() % 2 == 0 ? 8 : 5;

        // Add row numbers
        for (int row = 1; row <= rows; row++) {
            Label rowLabel = new Label(String.valueOf(row));
            rowLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15;");
            seatGrid.add(rowLabel, 0, row);
        }

        // Add seats
        for (Seat seat : screening.getSeats()) {
            StackPane seatPane = createSeatPane(seat);
            seatGrid.add(seatPane, seat.getSeatNumber(), seat.getSeatRow());
        }

        rightPane.getChildren().add(seatGrid);
    }

    private void initializeMovieDetails() {
        movieTitleLabel.setText(screening.getMovie().getEnglishName());
        cinemaLabel.setText(screening.getCinema().getCinemaName());
        screeningTimeLabel.setText(screening.getTime().toString());

        // Load movie image
        String imagePath = screening.getMovie().getMovieIconAsString();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                if (image.isError()) {
                    System.err.println("Error loading image: " + image.getException().getMessage());
                } else {
                    movieImageView.setImage(image);
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Image path is null or empty");
        }
    }

    @FXML
    private void handleBackButton() {
        try {
            cleanup();
            App.setRoot("CinemaMovieDetails", screening.getMovie());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error returning to movie details.");
        }
    }

    private StackPane createScreenRectangle() {
        Rectangle screen = new Rectangle(400, 30);
        screen.setFill(Color.GRAY);
        screen.setArcHeight(10);
        screen.setArcWidth(10);

        Label screenLabel = new Label("Screen");
        screenLabel.setTextFill(Color.BLACK);
        screenLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        screenLabel.setTextFill(Color.WHITE);

        StackPane screenPane = new StackPane(screen, screenLabel);
        return screenPane;
    }

    private StackPane createSeatPane(Seat seat) {
        Rectangle rectangle = new Rectangle(30, 35);
        rectangle.setFill(seat.isAvailable() ? Color.GREEN : Color.GRAY);
        //rectangle.setStroke(Color.BLACK);
        rectangle.setArcHeight(5);
        rectangle.setArcWidth(5);

        Label seatLabel = new Label(String.valueOf(seat.getSeatNumber()));
        seatLabel.setTextFill(Color.WHITE);
        seatLabel.setStyle("-fx-font-size: 13;");

        StackPane stackPane = new StackPane(rectangle, seatLabel);
        if (seat.isAvailable()) {
            stackPane.setOnMouseClicked(event -> handleSeatSelection(seat, stackPane));
        }

        return stackPane;
    }

    private void handleSeatSelection(Seat seat, StackPane seatPane) {
        if (!seat.isAvailable()) {
            return;  // Don't allow selection of unavailable seats
        }

        if (selectedSeats.contains(seat)) {
            selectedSeats.remove(seat);
            ((Rectangle) seatPane.getChildren().get(0)).setFill(Color.GREEN);
        } else {
            selectedSeats.add(seat);
            ((Rectangle) seatPane.getChildren().get(0)).setFill(Color.web("#e43939"));
        }
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        totalPrice = selectedSeats.size() * screening.getMovie().getCinemaPrice();
        totalPriceLabel.setText("Total Price: ₪" + totalPrice);
    }

    @FXML
    private void handlePurchase() {
        if (selectedSeats.isEmpty()) {
            showAlert("Please select at least one seat.");
            return;
        }

        TicketPurchaseInfo purchaseInfo = new TicketPurchaseInfo(screening, selectedSeats, totalPrice);
        // The purchase time is automatically set in the TicketPurchaseInfo constructor

        try {
            cleanup();
            App.setRoot("ticketsPayment", purchaseInfo);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error proceeding to payment.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Purchase Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void initialize() {
        EventBus.getDefault().register(this);
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}