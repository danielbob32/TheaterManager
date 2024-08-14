package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.events.MovieUpdateEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class GeneralMovieDetailsController implements DataInitializable {

    @FXML private ImageView movieImage;
    @FXML private Label englishTitleLabel;
    @FXML private Label hebrewTitleLabel;
    @FXML private Label producerLabel;
    @FXML private Label actorsLabel;
    @FXML private Label durationLabel;
    @FXML private Label genreLabel;
    @FXML private TextArea synopsisArea;
    @FXML private Label movieTypeLabel;
    @FXML private CheckBox cinemaCheckBox;
    @FXML private CheckBox homeCheckBox;
    @FXML private Button saveChangesButton;

    private SimpleClient client;
    private Movie currentMovie;

    public void initialize() {
        EventBus.getDefault().register(this);
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
        }
    }

    private void displayMovieDetails() {
        englishTitleLabel.setText(currentMovie.getEnglishName());
        hebrewTitleLabel.setText(currentMovie.getHebrewName());
        producerLabel.setText("Producer: " + currentMovie.getProducer());
        actorsLabel.setText("Actors: " + currentMovie.getActors());
        durationLabel.setText("Duration: " + currentMovie.getDuration() + " minutes");
        genreLabel.setText("Genre: " + currentMovie.getGenre());
        synopsisArea.setText(currentMovie.getSynopsis());

        movieImage.setImage(convertByteArrayToImage(currentMovie.getMovieIcon()));

        updateMovieTypeLabel();

        cinemaCheckBox.setSelected(currentMovie.getIsCinema());
        homeCheckBox.setSelected(currentMovie.getIsHome());

        cinemaCheckBox.setOnAction(e -> updateMovieTypeLabel());
        homeCheckBox.setOnAction(e -> updateMovieTypeLabel());
    }

    private void updateMovieTypeLabel() {
        boolean isCinema = cinemaCheckBox.isSelected();
        boolean isHome = homeCheckBox.isSelected();
        boolean isComingSoon = isComingSoon();

        if (isCinema && isHome) {
            movieTypeLabel.setText("Cinema & Home Movie");
        } else if (isCinema) {
            movieTypeLabel.setText("Cinema Movie");
        } else if (isHome) {
            movieTypeLabel.setText("Home Movie");
        } else if (isComingSoon) {
            movieTypeLabel.setText("Coming Soon Movie");
        } else {
            movieTypeLabel.setText("Not Available");
        }
    }

    private boolean isComingSoon() {
        LocalDate premierDate = currentMovie.getPremier().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return !cinemaCheckBox.isSelected() && !homeCheckBox.isSelected() && premierDate.isAfter(LocalDate.now());
    }

    @FXML
    private void handleSaveChanges() {
        currentMovie.setIsCinema(cinemaCheckBox.isSelected());
        currentMovie.setIsHome(homeCheckBox.isSelected());
        try {
            client.updateMovie(currentMovie);
//            showAlert("Success", "Movie details updated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update movie details. Please try again.");
        }
    }

    @FXML
    private void handleBackButton() throws IOException {
        App.setRoot("AllMoviesListView", null);
    }

    private Image convertByteArrayToImage(byte[] imageData) {
        if (imageData != null && imageData.length > 0) {
            return new Image(new ByteArrayInputStream(imageData));
        }
        return null;
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    @Subscribe
    public void onMovieUpdateEvent(MovieUpdateEvent event) {
        if (event.isSuccess()) {
            showAlert("Success", "Movie updated successfully.");
        } else {
            showAlert("Error", "Failed to update movie. Please try again.");
        }
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}