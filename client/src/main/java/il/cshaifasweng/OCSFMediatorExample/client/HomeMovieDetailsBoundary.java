package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.events.MessageEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Movie;
import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextFlow;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HomeMovieDetailsBoundary implements DataInitializable {

    @FXML private Label englishTitleLabel;
    @FXML private Label hebrewTitleLabel;
    @FXML private Label producerLabel;
    @FXML private Label actorsLabel;
    @FXML private Label durationLabel;
    @FXML private Label genreLabel;
    @FXML private TextFlow synopsisArea;
    @FXML private ImageView movieImage;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private DatePicker dateSelector;
    @FXML private Button buyLinkButton;
    @FXML private Button backButton;

    private SimpleClient client;
    private Movie currentMovie;
    private boolean isContentManager = false;

    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);
        
        LocalDate today = LocalDate.now();
        dateSelector.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });
        dateSelector.setValue(today);
        
        populateTimeComboBox();
        
        dateSelector.valueProperty().addListener((observable, oldValue, newValue) -> handleDateChange());
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
        if (data instanceof Movie) {
            currentMovie = (Movie) data;
            displayMovieDetails();
            populateTimeComboBox();
            checkUserPermissions();
        }
    }

    private void checkUserPermissions() {
        Person connectedPerson = client.getConnectedPerson();
        isContentManager = (connectedPerson instanceof Worker) &&
                ((Worker) connectedPerson).getWorkerType().equals("Content manager");

        buyLinkButton.setVisible(!isContentManager);
    }

    private void displayMovieDetails() {
        englishTitleLabel.setText(currentMovie.getEnglishName());
        hebrewTitleLabel.setText(currentMovie.getHebrewName());
        producerLabel.setText("Producer: " + currentMovie.getProducer());
        actorsLabel.setText("Actors: " + currentMovie.getActors());
        durationLabel.setText("Duration: " + currentMovie.getDuration() + " minutes");
        genreLabel.setText("Genres: " + String.join(", ", currentMovie.getGenres()));
        synopsisArea.getChildren().clear();
        synopsisArea.getChildren().add(new Label(currentMovie.getSynopsis()));

        byte[] imageData = currentMovie.getMovieIcon();
        Image image = convertByteArrayToImage(imageData);
        if (image == null || image.isError()) {
            image = loadDefaultImage();
        }
        movieImage.setImage(image);
    }

    private Image convertByteArrayToImage(byte[] imageData) {
        if (imageData != null && imageData.length > 0) {
            return new Image(new ByteArrayInputStream(imageData));
        }
        return null;
    }

    private Image loadDefaultImage() {
        try {
            InputStream defaultImageStream = getClass().getClassLoader().getResourceAsStream("Images/default.jpg");
            if (defaultImageStream != null) {
                return new Image(defaultImageStream);
            }
        } catch (Exception e) {
            System.out.println("Error loading default image: " + e.getMessage());
        }
        return null;
    }

    private void populateTimeComboBox() {
        LocalTime now = LocalTime.now();
        LocalTime startTime = now.plusMinutes(30 - now.getMinute() % 30);
        
        // Ensure the start time is at least 09:00
        if (startTime.isBefore(LocalTime.of(9, 0))) {
            startTime = LocalTime.of(9, 0);
        }
        
        LocalTime endTime = LocalTime.of(23, 30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        timeComboBox.getItems().clear();
        while (startTime.isBefore(endTime) || startTime.equals(endTime)) {
            timeComboBox.getItems().add(startTime.format(formatter));
            startTime = startTime.plusMinutes(30);
        }

        if (!timeComboBox.getItems().isEmpty()) {
            timeComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleDateChange() {
        LocalDate selectedDate = dateSelector.getValue();
        LocalDate today = LocalDate.now();
    
        // Clear the time combo box before repopulating
        timeComboBox.getItems().clear();
    
        if (selectedDate == null) {
            return;
        }
    
        Platform.runLater(() -> {
            try {
                if (selectedDate.isEqual(today)) {
                    populateTimeComboBox();
                } else if (selectedDate.isAfter(today)) {
                    populateFullTimeComboBox();
                } else {
                    showAlert("Invalid Date", "Please select today or a future date.");
                    dateSelector.setValue(today);
                    populateTimeComboBox();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "An unexpected error occurred. Please try again.");
            }
        });
    }
    
    
    
    private void populateFullTimeComboBox() {
        LocalTime startTime = LocalTime.of(0, 0);
        LocalTime endTime = LocalTime.of(23, 30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        while (startTime.isBefore(endTime) || startTime.equals(endTime)) {
            timeComboBox.getItems().add(startTime.format(formatter));
            startTime = startTime.plusMinutes(30);
        }

        if (!timeComboBox.getItems().isEmpty()) {
            timeComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleBuyLink() throws IOException {
        String selectedTime = timeComboBox.getValue();
        String selectedDate = dateSelector.getValue() != null ? dateSelector.getValue().toString() : "";

        if (selectedTime != null && !selectedDate.isEmpty()) {
            cleanup();
            App.setRoot("PurchaseLinkBoundary", new Object[]{currentMovie, selectedTime, selectedDate});
        } else {
            showAlert("Missing Information", "Please select a time and date before purchasing a link.");
        }
    }

    @FXML
    private void handleBackButton() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
        App.setRoot("HomeMovieList", connectedPerson);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Subscribe
    public void onMessageEvent(MessageEvent event) {
        Platform.runLater(this::displayMovieDetails);
    }
}
