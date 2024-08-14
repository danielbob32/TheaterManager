package il.cshaifasweng.OCSFMediatorExample.client;
import il.cshaifasweng.OCSFMediatorExample.client.events.MovieListEvent;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
        movieBox.setPadding(new Insets(10));
        movieBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5;");

        HBox contentBox = new HBox(10);
        contentBox.prefWidthProperty().bind(moviesContainer.widthProperty().multiply(0.80));

        ImageView iv = new ImageView(convertByteArrayToImage(movie.getMovieIcon()));
        iv.setFitWidth(150);
        iv.setFitHeight(200);
        iv.setPreserveRatio(true);

        VBox textContent = new VBox(5);
        Label titleLabel = new Label(movie.getEnglishName());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15;");
        Label typeLabel = new Label(getMovieType(movie));

        textContent.getChildren().addAll(titleLabel, typeLabel);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Button moviePageButton = new Button("Movie Page");
        moviePageButton.setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15;");
        moviePageButton.setCursor(Cursor.HAND);
        moviePageButton.setOnAction(e -> {
            try {
                openMoviePage(movie);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        VBox buttonBox = new VBox(moviePageButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(buttonBox, Priority.NEVER);

        contentBox.getChildren().addAll(iv, textContent, buttonBox);

        movieBox.getChildren().add(contentBox);
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