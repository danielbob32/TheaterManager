package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import javafx.fxml.FXML;
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
//        if (!"Content manager".equals(workerType) && !"Chain manager".equals(workerType)) {
//            // If not a content manager or chain manager, go back to the worker menu
//            try {
//                Person connectedPerson = client.getConnectedPerson();
//                App.setRoot("WorkerMenu", connectedPerson);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        System.out.println("UpdateContentController initialized for " + workerType);
    }

    @FXML
    private void editCinemaMovies() throws IOException {
        App.setRoot("CinemaMovieList", workerType);
    }

    @FXML
    private void editHomeMovies() throws IOException {
        App.setRoot("HomeMovieList", workerType);
    }

    @FXML
    private void addMovie() throws IOException {
        App.setRoot("AddMovie", workerType);
    }

    @FXML
    private void updatePrices() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        App.setRoot("UpdatePrices", connectedPerson);
    }

    @FXML
    private void goBack() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        App.setRoot("WorkerMenu", workerType);
    }



}

