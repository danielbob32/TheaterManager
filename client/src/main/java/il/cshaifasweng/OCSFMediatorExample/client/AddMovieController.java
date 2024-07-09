package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import il.cshaifasweng.OCSFMediatorExample.entities.HomeMovieLink;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Movie;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class AddMovieController {

    private ObjectMapper objectMapper = new ObjectMapper();


    @FXML private AnchorPane mainContainer;
    @FXML private TextField englishNameField;
    @FXML private TextField hebrewNameField;
    @FXML private TextField producerField;
    @FXML private TextField actorsField;
    @FXML private TextField durationField;
    @FXML private TextField movieIconField;
    @FXML private TextArea synopsisArea;
    @FXML private TextField genreField;
    @FXML private DatePicker premierDatePicker;
    @FXML private CheckBox isCinemaCheckBox;
    @FXML private VBox cinemaFields;
    @FXML private CheckBox isHomeCheckBox;
    @FXML private VBox homeMovieFields;
    @FXML private TextField watchLinkField;
    @FXML private TextField cinemaPriceField;
    @FXML private TextField homePriceField;

    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);
    }

    @FXML
    private void toggleHomeMovieFields() {
        boolean isHomeMovie = isHomeCheckBox.isSelected();
        homeMovieFields.setVisible(isHomeMovie);
        homeMovieFields.setManaged(isHomeMovie);
    }

    @FXML
    private void toggleCinemaFields() {
        boolean isCinemaMovie = isCinemaCheckBox.isSelected();
        cinemaFields.setVisible(isCinemaMovie);
        cinemaFields.setManaged(isCinemaMovie);
    }

    @FXML
    private void submitMovie() {
        try {
            String englishName = validateField(englishNameField, "English Name");
            String hebrewName = validateField(hebrewNameField, "Hebrew Name");
            String producer = validateField(producerField, "Producer");
            String actors = validateField(actorsField, "Actors");
            int duration = Integer.parseInt(validateField(durationField, "Duration"));
            String movieIcon = validateField(movieIconField, "Movie Icon URL");
            String synopsis = validateField(synopsisArea, "Synopsis");
            String genre = validateField(genreField, "Genre");
            LocalDate premierDate = validateDatePicker(premierDatePicker, "Premier Date");
            boolean isCinema = isCinemaCheckBox.isSelected();
            boolean isHome = isHomeCheckBox.isSelected();

            int cinemaPrice = 0;
            if (isCinema) {
                cinemaPrice = Integer.parseInt(validateField(cinemaPriceField, "Cinema Price"));
            }

            int homePrice = 0;
            String watchLink = "null";
            if (isHome) {
                homePrice = Integer.parseInt(validateField(homePriceField, "Home Movie Price"));
                watchLink = validateField(watchLinkField, "Watch Link");
            }

            Date premier = Date.from(premierDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Movie movie = new Movie(englishName, hebrewName, producer, actors, duration, movieIcon, synopsis, genre, premier, isHome, isCinema, cinemaPrice, homePrice);

            if (isHome) {
                HomeMovieLink homeMovieLink = new HomeMovieLink(null, null, true, watchLink, 0, homePrice, true, null, null);
                String movieData = objectMapper.writeValueAsString(movie);
                String homeMovieLinkData = objectMapper.writeValueAsString(homeMovieLink);

                Message msg = new Message(0, "add home movie", movieData, homeMovieLinkData);
                SimpleClient.getClient().sendToServer(msg);
            } else {
                String movieData = objectMapper.writeValueAsString(movie);
                Message msg = new Message(0, "add movie", movieData);
                SimpleClient.getClient().sendToServer(msg);
            }

            clearForms();
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for Duration and Price fields.");
        } catch (IOException e) {
            showAlert("Error", "An error occurred while sending the movie data.");
            e.printStackTrace();
        } catch (ValidationException e) {
            showAlert("Validation Error", e.getMessage());
        }
    }

    @FXML
    private void goBack() throws IOException {
        App.setRoot("UpdateContent");
    }

    @Subscribe
    public void onWarningEvent(WarningEvent event) {
        Platform.runLater(() -> {
            String message = event.getWarning().getMessage();
            if (message.contains("added successfully")) {
                showAlert("Success", message);
                clearForms();
            } else {
                showAlert("Warning", message);
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String validateField(TextInputControl field, String fieldName) throws ValidationException {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
        return value;
    }

    private LocalDate validateDatePicker(DatePicker datePicker, String fieldName) throws ValidationException {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            throw new ValidationException(fieldName + " must be selected.");
        }
        return date;
    }

    private static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        // Update the window title or any other UI element showing the "Login Failed" message
        Platform.runLater(() -> {
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.setTitle("Add Movie/Home Movie");
        });
    }

    private void clearForms() {

        // Clear Movie form fields
        englishNameField.clear();
        hebrewNameField.clear();
        producerField.clear();
        actorsField.clear();
        durationField.clear();
        movieIconField.clear();
        synopsisArea.clear();
        genreField.clear();
        premierDatePicker.setValue(null);
        isHomeCheckBox.setSelected(false);
        cinemaFields.setVisible(false);
        cinemaFields.setManaged(false);
        isCinemaCheckBox.setSelected(false);
        homeMovieFields.setVisible(false);
        homeMovieFields.setManaged(false);

        // Clear Home Movie form fields
        watchLinkField.clear();
        cinemaPriceField.clear();
        homePriceField.clear();
    }
}