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
    @FXML private ComboBox<String> movieTypeComboBox;
    @FXML private VBox movieForm;
    @FXML private VBox homeMovieForm;
    @FXML private TextField englishNameField;
    @FXML private TextField hebrewNameField;
    @FXML private TextField producerField;
    @FXML private TextField actorsField;
    @FXML private TextField durationField;
    @FXML private TextField movieIconField;
    @FXML private TextArea synopsisArea;
    @FXML private TextField genreField;
    @FXML private DatePicker premierDatePicker;
    @FXML private CheckBox isHomeCheckBox;
    @FXML private DatePicker openTimeDatePicker;
    @FXML private DatePicker closeTimeDatePicker;
    @FXML private TextField watchLinkField;
    @FXML private TextField priceField;
    @FXML private DatePicker purchaseTimeDatePicker;

    @FXML
    public void initialize() {
        try {
            EventBus.getDefault().register(this);
            movieTypeComboBox.getItems().addAll("Regular Movie", "Home Movie");
            movieForm.setVisible(false);
            movieForm.setManaged(false);
            homeMovieForm.setVisible(false);
            homeMovieForm.setManaged(false);
            System.out.println("AddMovieController initialized successfully");

            Platform.runLater(() -> {
                System.out.println("Main container width: " + mainContainer.getWidth());
                System.out.println("Main container height: " + mainContainer.getHeight());
                System.out.println("ComboBox items: " + movieTypeComboBox.getItems());
            });
        } catch (Exception e) {
            System.err.println("Error initializing AddMovieController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMovieTypeSelection() {
        String selectedType = movieTypeComboBox.getValue();
        System.out.println("Selected type: " + selectedType);
        if ("Regular Movie".equals(selectedType)) {
            movieForm.setVisible(true);
            movieForm.setManaged(true);
            homeMovieForm.setVisible(false);
            homeMovieForm.setManaged(false);
        } else if ("Home Movie".equals(selectedType)) {
            movieForm.setVisible(false);
            movieForm.setManaged(false);
            homeMovieForm.setVisible(true);
            homeMovieForm.setManaged(true);
        }
        System.out.println("Movie form visible: " + movieForm.isVisible());
        System.out.println("Home movie form visible: " + homeMovieForm.isVisible());
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
            boolean isHome = isHomeCheckBox.isSelected();
            boolean isCinema = true; // Always true for regular movies

            Date premier = Date.from(premierDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Movie movie = new Movie(englishName, hebrewName, producer, actors, duration, movieIcon, synopsis, genre, premier, isHome, isCinema);

            String movieData = objectMapper.writeValueAsString(movie);
            Message msg = new Message(0, "add movie", movieData);
            SimpleClient.getClient().sendToServer(msg);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for the Duration field.");
        } catch (IOException e) {
            showAlert("Error", "An error occurred while sending the movie data.");
            e.printStackTrace();
        } catch (ValidationException e) {
            showAlert("Validation Error", e.getMessage());
        }
    }

    @FXML
    private void submitHomeMovie() {
        try {
            LocalDate openTime = validateDatePicker(openTimeDatePicker, "Open Time");
            LocalDate closeTime = validateDatePicker(closeTimeDatePicker, "Close Time");
            boolean isOpen = true; // Always true for home movies
            String watchLink = validateField(watchLinkField, "Watch Link");
            int price = Integer.parseInt(validateField(priceField, "Price"));
            LocalDate purchaseTime = validateDatePicker(purchaseTimeDatePicker, "Purchase Time");

            Date open = Date.from(openTime.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date close = Date.from(closeTime.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date purchase = Date.from(purchaseTime.atStartOfDay(ZoneId.systemDefault()).toInstant());

            HomeMovieLink homeMovieLink = new HomeMovieLink(open, close, isOpen, watchLink, 0, price, true, null, purchase);

            String homeMovieLinkData = objectMapper.writeValueAsString(homeMovieLink);
            Message msg = new Message(0, "add home movie", homeMovieLinkData);
            SimpleClient.getClient().sendToServer(msg);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for the Price field.");
        } catch (IOException e) {
            showAlert("Error", "An error occurred while sending the home movie data.");
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
        // Reset ComboBox and hide forms
        movieTypeComboBox.setValue(null);
        movieForm.setVisible(false);
        movieForm.setManaged(false);
        homeMovieForm.setVisible(false);
        homeMovieForm.setManaged(false);

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

        // Clear Home Movie form fields
        openTimeDatePicker.setValue(null);
        closeTimeDatePicker.setValue(null);
        watchLinkField.clear();
        priceField.clear();
        purchaseTimeDatePicker.setValue(null);
    }
}