package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import javafx.fxml.FXML;
import java.io.IOException;

public class UpdateContentController implements DataInitializable {

    private SimpleClient client;

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        System.out.println("CustomerMenuController initialized");
    }

    @FXML
    private void deleteMovies() throws IOException {
        App.setRoot("CinemaMovieList", null);
    }

    @FXML
    private void deleteHomeMovies() throws IOException {
        App.setRoot("CinemaHomeMovieList", null);
    }

    @FXML
    private void addMovie() throws IOException {
        App.setRoot("AddMovie", null);
    }

    @FXML
    private void updateShowtimes() throws IOException {
        App.setRoot("UpdateShowtimes", null);
    }

    @FXML
    private void updatePrices() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        App.setRoot("UpdatePrices", connectedPerson);
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
                Person connectedPerson = client.getConnectedPerson();
                App.setRoot("WorkerMenu", connectedPerson);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}