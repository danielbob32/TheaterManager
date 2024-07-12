package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.fxml.FXML;
import java.io.IOException;

public class UpdateContentController {

    @FXML
    private void deleteMovies() throws IOException {
        App.setRoot("CinemaMovieList");
    }

    @FXML
    private void deleteHomeMovies() throws IOException {
        App.setRoot("CinemaHomeMovieList");
    }

    @FXML
    private void addMovie() throws IOException {
        App.setRoot("AddMovie");
    }

    @FXML
    private void updateShowtimes() throws IOException {
        App.setRoot("UpdateShowtimes");
    }

    @FXML
    private void updatePrices() throws IOException {
        App.setRoot("UpdatePrices");
    }

    @FXML
    private void goBack() throws IOException {
        App.setRoot("WorkerMenu");
    }

    public void initialize() {
        // Check if the current user is a content manager
        if (!"Content manager".equals(WorkerMenuController.getWorkerType())) {
            // If not, go back to the worker menu
            try {
                App.setRoot("WorkerMenu");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}