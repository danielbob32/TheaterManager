package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Movie;
import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.CheckComboBox;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AddMovieController implements DataInitializable {

    private SimpleClient client;

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        System.out.println("AddMovieController initialized");
    }

    private ObjectMapper objectMapper = new ObjectMapper();


    @FXML private AnchorPane mainContainer;
    @FXML private TextField englishNameField;
    @FXML private TextField hebrewNameField;
    @FXML private TextField producerField;
    @FXML private TextField actorsField;
    @FXML private TextField durationField;
    @FXML private Button uploadImageButton;
    @FXML private TextArea synopsisArea;
    @FXML private CheckComboBox<String> genreCheckComboBox;
    @FXML private DatePicker premierDatePicker;
    @FXML private CheckBox isCinemaCheckBox;
    @FXML private VBox cinemaFields;
    @FXML private CheckBox isHomeCheckBox;
    @FXML private VBox homeMovieFields;
    @FXML private TextField cinemaPriceField;
    @FXML private TextField homePriceField;
    private File selectedImageFile;

    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);
        // Initialize the genre options
        ObservableList<String> genres = FXCollections.observableArrayList(
                "Action", "Drama", "Comedy", "Fantasy", "Sci-Fi", "Romance", "Horror", "Adventure", "Animation", "Documentary", "Family", "Children"
        );
        genreCheckComboBox.getItems().addAll(genres);
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Movie Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        selectedImageFile = fileChooser.showOpenDialog(mainContainer.getScene().getWindow());
        if (selectedImageFile != null) {
            uploadImageButton.setText(selectedImageFile.getName());
        }
    }

    @FXML
    private void toggleHomeMovieFields() {
        boolean isHomeMovie = isHomeCheckBox.isSelected();
        //homeMovieFields.setVisible(isHomeMovie);
        //homeMovieFields.setManaged(isHomeMovie);
    }

    @FXML
    private void toggleCinemaFields() {
        boolean isCinemaMovie = isCinemaCheckBox.isSelected();
        //cinemaFields.setVisible(isCinemaMovie);
        //cinemaFields.setManaged(isCinemaMovie);
    }

    @FXML
    private void submitMovie() {
        try {
            String englishName = validateField(englishNameField, "English Name");
            String hebrewName = validateField(hebrewNameField, "Hebrew Name");
            String producer = validateField(producerField, "Producer");
            String actors = validateField(actorsField, "Actors");
            int duration = Integer.parseInt(validateField(durationField, "Duration"));

            byte[] movieIcon = null;
            if (selectedImageFile != null) {
                try {
                    movieIcon = java.nio.file.Files.readAllBytes(selectedImageFile.toPath());
                } catch (IOException e) {
                    throw new ValidationException("Error reading image file.");
                }
            }
            String synopsis = validateField(synopsisArea, "Synopsis");
            // Collect selected genres
            List<String> selectedGenres = genreCheckComboBox.getCheckModel().getCheckedItems().stream().collect(Collectors.toList());

            LocalDate premierDate = validateDatePicker(premierDatePicker, "Premier Date");
            boolean isCinema = isCinemaCheckBox.isSelected();
            boolean isHome = isHomeCheckBox.isSelected();

            int cinemaPrice = 0;
            cinemaPrice = Integer.parseInt(validateField(cinemaPriceField, "Cinema Price"));

            int homePrice = 0;
            homePrice = Integer.parseInt(validateField(homePriceField, "Home Movie Price"));

            Date premier = Date.from(premierDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Movie movie = new Movie(englishName, hebrewName, producer, actors, duration, movieIcon, synopsis, selectedGenres, premier, isHome, isCinema, cinemaPrice, homePrice);
            String movieData = objectMapper.writeValueAsString(movie);

            Message msg = new Message(0, "add movie", movieData);
            client.sendToServer(msg);

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
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
        App.setRoot("UpdateContent", connectedPerson);
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onWarningEvent(WarningEvent event) {
        Platform.runLater(() -> {
            String message = event.getWarning().getMessage();
            if (message.contains("added successfully")) {
                showAlert("Success", message);
                clearForms();
            } else if (message.contains("already exists")) {
                showAlert("Warning", "This movie already exists in the database.");
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
        synopsisArea.clear();
        genreCheckComboBox.getCheckModel().clearChecks();  // Clear selected genres
        premierDatePicker.setValue(null);
        isHomeCheckBox.setSelected(false);
        cinemaFields.setVisible(true);
        cinemaFields.setManaged(true);
        isCinemaCheckBox.setSelected(false);
        homeMovieFields.setVisible(true);
        homeMovieFields.setManaged(true);
        cinemaPriceField.clear();
        homePriceField.clear();
        uploadImageButton.setText("Upload Image");
        selectedImageFile = null;
    }
}