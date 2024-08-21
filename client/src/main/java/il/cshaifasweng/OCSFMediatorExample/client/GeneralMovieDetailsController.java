package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import il.cshaifasweng.OCSFMediatorExample.client.events.MovieUpdateEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Movie;
import il.cshaifasweng.OCSFMediatorExample.entities.PriceChangeRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

public class GeneralMovieDetailsController implements DataInitializable {

    @FXML private ImageView movieImageView;
    @FXML private Label englishNameLabel;
    @FXML private Label hebrewNameLabel;
    @FXML private Label producerLabel;
    @FXML private Label actorsLabel;
    @FXML private TextArea synopsisTextArea;
    @FXML private CheckBox cinemaCheckBox;
    @FXML private CheckBox homeCheckBox;
    @FXML private Label cinemaPriceLabel;
    @FXML private Label homePriceLabel;
    @FXML private TextField cinemaPriceInput;
    @FXML private TextField homePriceInput;
    @FXML private Button saveChangesButton;
    @FXML private VBox priceChangeContainer;

    private SimpleClient client;
    private Movie movie;
    private ObjectMapper objectMapper;

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        if (data instanceof Movie) {
            this.movie = (Movie) data;
            displayMovieDetails();
        }
        objectMapper = new ObjectMapper();
    }

    public void initialize() {
        EventBus.getDefault().register(this);
    }

    private void displayMovieDetails() {
        Image image = new Image(new ByteArrayInputStream(movie.getMovieIcon()));
        movieImageView.setImage(image);
        englishNameLabel.setText("English Name: " + movie.getEnglishName());
        hebrewNameLabel.setText("Hebrew Name: " + movie.getHebrewName());
        producerLabel.setText("Producer: " + movie.getProducer());
        actorsLabel.setText("Main Actors: " + movie.getActors());
        synopsisTextArea.setText(movie.getSynopsis());
        cinemaCheckBox.setSelected(movie.getIsCinema());
        homeCheckBox.setSelected(movie.getIsHome());
        cinemaPriceLabel.setText("Cinema Price: ₪" + movie.getCinemaPrice());
        homePriceLabel.setText("Home Price: ₪" + movie.getHomePrice());

        // Disable price inputs if the corresponding checkbox is not selected
        cinemaPriceInput.setText(String.valueOf(movie.getCinemaPrice()));
        homePriceInput.setText(String.valueOf(movie.getHomePrice()));
        cinemaPriceInput.setDisable(!movie.getIsCinema());
        homePriceInput.setDisable(!movie.getIsHome());

        setupAccessControl();
        setupListeners();
    }

    private void setupAccessControl() {
        boolean isContentOrChainManager = false;
        if (client.getConnectedPerson() instanceof Worker) {
            String workerType = ((Worker) client.getConnectedPerson()).getWorkerType();
            isContentOrChainManager = "Content manager".equals(workerType) || "Chain manager".equals(workerType);
        }

        cinemaCheckBox.setDisable(!isContentOrChainManager);
        homeCheckBox.setDisable(!isContentOrChainManager);

        // Disable price inputs based on the initial state of the checkboxes
        cinemaPriceInput.setDisable(!isContentOrChainManager || !movie.getIsCinema());
        homePriceInput.setDisable(!isContentOrChainManager || !movie.getIsHome());

        saveChangesButton.setVisible(isContentOrChainManager);
        priceChangeContainer.setVisible(isContentOrChainManager);
    }

    private void setupListeners() {
        cinemaCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            cinemaPriceInput.setDisable(!newValue);
        });

        homeCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            homePriceInput.setDisable(!newValue);
        });
    }


    @FXML
    private void handleSaveChanges() {
        boolean isCinemaChanged = cinemaCheckBox.isSelected() != movie.getIsCinema();
        boolean isHomeChanged = homeCheckBox.isSelected() != movie.getIsHome();
        boolean cinemaPriceChanged = !cinemaPriceInput.getText().equals(String.valueOf(movie.getCinemaPrice()));
        boolean homePriceChanged = !homePriceInput.getText().equals(String.valueOf(movie.getHomePrice()));

        if (isCinemaChanged || isHomeChanged || cinemaPriceChanged || homePriceChanged) {
            movie.setIsCinema(cinemaCheckBox.isSelected());
            movie.setIsHome(homeCheckBox.isSelected());

            try {
                if (cinemaPriceChanged) {
                    int newCinemaPrice = Integer.parseInt(cinemaPriceInput.getText());
                    sendPriceChangeRequest("Cinema Movies", movie.getCinemaPrice(), newCinemaPrice);
                    movie.setCinemaPrice(newCinemaPrice);
                }

                if (homePriceChanged) {
                    int newHomePrice = Integer.parseInt(homePriceInput.getText());
                    sendPriceChangeRequest("Home Movies", movie.getHomePrice(), newHomePrice);
                    movie.setHomePrice(newHomePrice);
                }

                Message updateMsg = new Message(0, "updateMovie", objectMapper.writeValueAsString(movie));
                client.sendToServer(updateMsg);
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter valid numbers for prices.", Alert.AlertType.ERROR);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to save changes.", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("No Changes", "No changes were made to the movie details.");
        }
    }

    private void sendPriceChangeRequest(String movieType, int oldPrice, int newPrice) throws IOException {
        PriceChangeRequest request = new PriceChangeRequest(movie, movieType, oldPrice, newPrice, new Date(), "Pending");
        Message requestMsg = new Message(0, "createPriceChangeRequest", objectMapper.writeValueAsString(request));
        client.sendToServer(requestMsg);
    }

    private void showAlert(String title, String content) {
        showAlert(title, content, Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    @Subscribe
    public void onMovieUpdateEvent(MovieUpdateEvent event) {
        System.out.println("in onMovieUpdateEvent");
        if (event.isSuccess()) {
            showAlert("Success", "Movie details have been updated successfully.");
        } else {
            showAlert("Error", "Failed to update movie. Please try again.");
        }
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

    @FXML
    private void handleBackButton() throws IOException {
        cleanup();
        App.setRoot("AllMoviesListView", null);
    }
}
