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

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HomeMovieDetailsBoundary implements DataInitializable {

    @FXML
    private Label englishTitleLabel;
    @FXML
    private Label hebrewTitleLabel;
    @FXML
    private Label producerLabel;
    @FXML
    private Label actorsLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Label genreLabel;
    @FXML
    private TextFlow synopsisArea;
    @FXML
    private ImageView movieImage;
    @FXML
    private ComboBox<String> timeComboBox;
    @FXML
    private DatePicker dateSelector;
    @FXML
    private Button buyLinkButton;
    @FXML
    private Button backButton;

    private SimpleClient client;
    private Movie currentMovie;
    private boolean isContentManager = false;

    public void initialize() {
        EventBus.getDefault().register(this);
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
        genreLabel.setText("Genre: " + currentMovie.getGenre());
        synopsisArea.getChildren().clear();
        synopsisArea.getChildren().add(new Label(currentMovie.getSynopsis()));

        String imageName = currentMovie.getMovieIcon();
        Image image;
        try {
            String imagePath = "/Images/" + imageName;
            if (getClass().getResource(imagePath) != null) {
                image = new Image(getClass().getResourceAsStream(imagePath));
            } else {
                System.out.println("Image file not found: " + imageName);
                image = new Image(getClass().getResourceAsStream("/Images/default.jpg"));
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + imageName);
            e.printStackTrace();
            image = new Image(getClass().getResourceAsStream("/Images/default.jpg"));
        }

        if (image.isError()) {
            System.out.println("Error loading image: " + imageName);
            image = new Image(getClass().getResourceAsStream("/Images/default.jpg"));
        }

        movieImage.setImage(image);
    }

    private void populateTimeComboBox() {
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(23, 30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        while (startTime.isBefore(endTime)) {
            timeComboBox.getItems().add(startTime.format(formatter));
            startTime = startTime.plusMinutes(30);
        }
    }

    @FXML
    private void handleBuyLink() throws IOException {
        String selectedTime = timeComboBox.getValue();
        String selectedDate = dateSelector.getValue() != null ? dateSelector.getValue().toString() : "";

        if (selectedTime != null && !selectedDate.isEmpty()) {
            App.setRoot("PurchaseLinkBoundary", new Object[]{currentMovie, selectedTime, selectedDate});
        } else {
            showAlert("Missing Information", "Please select a time and date before purchasing a link.");
        }
    }

    @FXML
    private void handleBackButton() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
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
