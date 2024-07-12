package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.events.MovieListEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import il.cshaifasweng.OCSFMediatorExample.entities.Movie;
import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;

public class UpdatePricesController implements DataInitializable {

    @FXML
    private ComboBox<String> movieTypeComboBox;
    @FXML
    private ComboBox<String> movieComboBox;
    @FXML
    private Label currentPriceLabel;
    @FXML
    private TextField newPriceField;

    private SimpleClient client;
    private List<Movie> movies;
    private String stringMovies;
    private Person connectedPerson;

    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);
        movieTypeComboBox.getItems().addAll("Cinema Movies", "Home Movies");
        movieTypeComboBox.setOnAction(event -> loadMovies());
        movieComboBox.setOnAction(event -> updateCurrentPrice());
    }

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        if (data instanceof Person) {
            this.connectedPerson = (Person) data;
            checkPermissions();
        }
    }

    private void checkPermissions() {
        if (!(connectedPerson instanceof Worker) ||
                !((Worker) connectedPerson).getWorkerType().equals("Content manager")) {
            showAlert("Access Denied", "You don't have permission to update prices.");
            try {
                goBack();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void loadMovies() {
        String movieType = movieTypeComboBox.getValue();
        System.out.println("loadMovies called with type: " + movieType);
        if (movieType != null) {
            try {
                System.out.println("Sending getMoviesForPrices request to server");
                client.sendToServer(new Message(0, "getMovies", movieType));
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to send getMoviesForPrices request: " + e.getMessage());
                showAlert("Error", "Failed to request movies from server");
            }
        } else {
            System.out.println("Movie type is null, not sending request");
        }
    }

    @Subscribe
    public void onMovieListEvent(MovieListEvent event) {
        stringMovies = event.getMoviesString();
        System.out.println("Received " + (movies != null ? movies.size() : 0) + " movies");

        // Check if the ComboBox is already empty
        if (!movieComboBox.getItems().isEmpty()) {
            movieComboBox.getItems().clear();
        }

        // Add the new movies only if the list is not empty
        if (movies != null && !movies.isEmpty()) {
            movieComboBox.getItems().add(stringMovies);
            System.out.println("Added " + movies.size() + " movies to ComboBox");
        } else {
            // Optionally, you can add a placeholder item or show a message
            movieComboBox.setPromptText("No movies available");
            System.out.println("No movies available");
        }
    }

//    private void updateCurrentPrice() {
//        Movie selectedMovie = movieComboBox.getValue();
//        if (selectedMovie != null) {
//            int price = movieTypeComboBox.getValue().equals("Cinema Movies") ?
//                    selectedMovie.getCinemaPrice() : selectedMovie.getHomePrice();
//            currentPriceLabel.setText("Current Price: " + price);
//        }
//    }

    @FXML
    private void updatePrice() {
        Movie selectedMovie = movieComboBox.getValue();
        String newPriceStr = newPriceField.getText();
        if (selectedMovie != null && !newPriceStr.isEmpty()) {
            try {
                int newPrice = Integer.parseInt(newPriceStr);
                String movieType = movieTypeComboBox.getValue();
                Message updateMsg = new Message(0, "updatePrice",
                        selectedMovie.getId() + "," + movieType + "," + newPrice);
                client.sendToServer(updateMsg);
            } catch (NumberFormatException e) {
                showAlert("Invalid price", "Please enter a valid number for the price.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void goBack() throws IOException {
        App.setRoot("UpdateContent");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Subscribe
    public void onWarningEvent(WarningEvent event) {
        showAlert("Update Result", event.getWarning().getMessage());
    }
}