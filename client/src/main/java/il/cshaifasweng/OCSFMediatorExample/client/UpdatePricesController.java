package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import il.cshaifasweng.OCSFMediatorExample.client.events.MovieListEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.PriceChangeRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class UpdatePricesController implements DataInitializable {

    @FXML
    private ComboBox<String> movieTypeComboBox;
    @FXML
    private ComboBox<Movie> movieComboBox;
    @FXML
    private Label currentPriceLabel;
    @FXML
    private TextField newPriceField;

    private SimpleClient client;
    private List<Movie> movies;
    private Person connectedPerson;
    private boolean isUpdating = false;
    private ObjectMapper objectMapper;
    private PriceChangeRequest request;


    @FXML
    public void initialize() {
        objectMapper = new ObjectMapper();
        EventBus.getDefault().register(this);
        movieTypeComboBox.getItems().addAll("Cinema Movies", "Home Movies");
        movieTypeComboBox.setOnAction(event -> loadMovies());
        movieComboBox.setOnAction(event -> updateCurrentPrice());
    }

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    public void initData(Object data) {
        if (data instanceof Person) {
            this.connectedPerson = (Person) data;
            checkPermissions();
        }
    }

    private void checkPermissions() {
        if (!(connectedPerson instanceof Worker) ||
                (!"Content manager".equals(((Worker) connectedPerson).getWorkerType()) &&
                        !"Chain manager".equals(((Worker) connectedPerson).getWorkerType()))) {
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
        try {
            client.sendToServer(new Message(0, "getMovies", movieType));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onMovieListEvent(MovieListEvent event) {
        System.out.println("in onMovieListEvent");
        Platform.runLater(() -> {
            movies = event.getMovies();

            if (!movieComboBox.getItems().isEmpty()) {
                movieComboBox.getItems().clear();
            }
            for (Movie movie : movieComboBox.getItems()) {
                System.out.println("movie in combo box: " + movie);
            }

            if (movies != null && !movies.isEmpty()) {
                for (Movie movie : movies) {
                    movieComboBox.getItems().add(movie);
                    System.out.println("movie price: " + movie.getCinemaPrice());
                }
                updateCurrentPrice();
            } else {
                movieComboBox.setPromptText("No movies available");
            }
        });
    }

    private void updateCurrentPrice() {
        Movie selectedMovie = movieComboBox.getValue();
        if (selectedMovie != null) {
            System.out.println("selected movie is:" + selectedMovie);
            String movieType = movieTypeComboBox.getValue();
            int price = movieType.equals("Cinema Movies") ? selectedMovie.getCinemaPrice() : selectedMovie.getHomePrice();
            System.out.println("new price is:" + price);
            currentPriceLabel.setText("Current Price: " + price);
        }
    }

    @FXML
    private void updatePrice() {
        Movie selectedMovie = movieComboBox.getValue();
        String newPriceStr = newPriceField.getText();
        if (selectedMovie != null && !newPriceStr.isEmpty()) {
            try {
                int newPrice = Integer.parseInt(newPriceStr);
                String movieType = movieTypeComboBox.getValue();
                int oldPrice = movieType.equals("Cinema Movies") ? selectedMovie.getCinemaPrice() : selectedMovie.getHomePrice();

                request = new PriceChangeRequest(selectedMovie, movieType, oldPrice, newPrice, new Date(), "Pending");

                Message requestMsg = new Message(0, "createPriceChangeRequest", this.objectMapper.writeValueAsString(request));
                client.sendToServer(requestMsg);

//                showAlert("Request Submitted", "Price change request has been submitted for approval.");
            } catch (NumberFormatException e) {
                showAlert("Invalid price", "Please enter a valid number for the price.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to submit price change request.");
            }
        }
    }

    @Subscribe
    public void onPriceChangeRequestEvent(PriceChangeRequestEvent event)
    {
        System.out.println("UpdatePricesController: PriceChangeRequestEvent: " + event.getMessage());
        if(event.getMessage().equals("Created"))
        {
            if(event.isSuccess() && request!=null && request.getRequestDate().equals(event.getRequest().getRequestDate()))
            {
                client.showSuccessAlert("The request was submitted successfully");
            }else {
                client.showErrorAlert("The request was not sent properly, please try again later!");
            }
        } else if (event.getMessage().equals("Approved") && event.isSuccess()){
            client.showAlert("A new approve or deny submitted to a request!", "updating the prices");
            loadMovies();
        }
    }

    @FXML
    private void goBack() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
        App.setRoot("UpdateContent", connectedPerson);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Subscribe
    public void onWarningEvent(WarningEvent event) {
        Platform.runLater(() -> {
            if (!isUpdating) {
                isUpdating = true;
                if (event.getWarning().getMessage().contains("successfully")) {
                    updateCurrentPrice(); 
                    loadMovies();
                }
                isUpdating = false;
            }
        });
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}