package il.cshaifasweng.OCSFMediatorExample.client;
import il.cshaifasweng.OCSFMediatorExample.client.events.MovieListEvent;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class AllMoviesListController implements DataInitializable {

    @FXML
    private VBox moviesContainer;

    private SimpleClient client;
    private List<Movie> allMovies;

    public void initialize() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        showAllMovies();
    }

    @FXML
    private void showAllMovies() {
        moviesContainer.getChildren().clear();
        client.getMovies(); // Assume this method is implemented in SimpleClient
    }

    @Subscribe
    public void onMovieListEvent(MovieListEvent event) {
        allMovies = event.getMovies();
        displayMovies(allMovies);
    }

    private void displayMovies(List<Movie> movies) {
        Platform.runLater(() -> {
            moviesContainer.getChildren().clear();
            for (Movie movie : movies) {
                VBox movieBox = createMovieBox(movie);
                moviesContainer.getChildren().add(movieBox);
            }
        });
    }

    private VBox createMovieBox(Movie movie) {
        VBox movieBox = new VBox(5);

        ImageView iv = new ImageView(convertByteArrayToImage(movie.getMovieIcon()));
        iv.setFitWidth(150);
        iv.setFitHeight(200);

        Label titleLabel = new Label(movie.getEnglishName());
        Label typeLabel = new Label(getMovieType(movie));

        Button moviePageButton = new Button("Movie Page");
        moviePageButton.setOnAction(e -> {
            try {
                openMoviePage(movie);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        movieBox.getChildren().addAll(iv, titleLabel, typeLabel, moviePageButton);
        return movieBox;
    }

    private String getMovieType(Movie movie) {
        if (movie.getIsCinema() && movie.getIsHome()) {
            return "Cinema & Home";
        } else if (movie.getIsCinema()) {
            return "Cinema";
        } else if (movie.getIsHome()) {
            return "Home";
        } else {
            return "Coming Soon";
        }
    }

    private Image convertByteArrayToImage(byte[] imageData) {
        if (imageData != null && imageData.length > 0) {
            return new Image(new ByteArrayInputStream(imageData));
        }
        return null;
    }

    private void openMoviePage(Movie movie) throws IOException {
        App.setRoot("GeneralMovieDetailsView", movie);
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

    @FXML
    private void handleBackButton(ActionEvent event) throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        if (connectedPerson instanceof Worker) {
            App.setRoot("UpdateContent", connectedPerson);
        } else if (connectedPerson instanceof Customer) {
            App.setRoot("customerMenu", connectedPerson);
        } else {
            App.setRoot("Loginpage", null);
        }
    }

}