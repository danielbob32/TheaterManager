package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import javafx.fxml.FXML;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class UpdateContentController implements DataInitializable {

    private SimpleClient client;
    private String workerType;

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        if (data instanceof String) {
            workerType = (String) data;
        }
    }

    @FXML
    private void editCinemaMovies() throws IOException {
        cleanup();
        App.setRoot("CinemaMovieList", workerType);
    }

    @FXML
    private void editHomeMovies() throws IOException {
        cleanup();
        App.setRoot("HomeMovieList", workerType);
    }

    @FXML
    private void addMovie() throws IOException {
        cleanup();
        App.setRoot("AddMovie", workerType);
    }

    @FXML
    private void updatePrices() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
        App.setRoot("UpdatePrices", connectedPerson);
    }

    @FXML
    private void allMoviesList() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
        App.setRoot("AllMoviesListView", connectedPerson);
    }

    @FXML
    private void goBack() throws IOException {
        cleanup();
        App.setRoot("WorkerMenu", workerType);
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

}

