package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.events.MovieListEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.Movie;
import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class HomeMoviesListBoundary implements DataInitializable {

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

    @FXML
    private void showHomeMovies() {
        System.out.println("in showHomeMovies");
        moviesContainer.getChildren().clear();
        client.getMovies();
    }

    @Override
    public void initData(Object data) {
        System.out.println("in initData with the next data" + data);

    }

    @Subscribe
    public void onMovieListEvent(MovieListEvent event) {
        System.out.println("in onMovieListEvent");
        allMovies = event.getMovies();
        displayHomeMovies();
    }

    private void displayHomeMovies() {
        List<Movie> homeMovies = allMovies.stream()
                .filter(movie -> movie.getIsHome())
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            moviesContainer.getChildren().clear();
            for (Movie movie : homeMovies) {
                VBox movieBox = createMovieBox(movie);
                moviesContainer.getChildren().add(movieBox);
            }
        });
    }

    private VBox createMovieBox(Movie movie) {
        System.out.println("in createMovieBox");
        VBox movieBox = new VBox(5);

        movieBox.setPadding(new Insets(10));
        movieBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5;");

        HBox contentBox = new HBox(10);
        contentBox.prefWidthProperty().bind(this.moviesContainer.widthProperty().multiply(0.80));

        String imageName = "deadpool.jpg";
        Image image = new Image(getClass().getResourceAsStream("/Images/" + imageName));
        ImageView iv = new ImageView(image);
        iv.setFitWidth(150);
        iv.setFitHeight(200);
        iv.setPreserveRatio(true);

        VBox textContent = new VBox(5);
        Label englishTitleLabel = new Label("English Title: " + movie.getEnglishName());
        Label hebrewTitleLabel = new Label("Hebrew Title: " + movie.getHebrewName());
        Label producerLabel = new Label("Producer: " + movie.getProducer());
        Label actorsLabel = new Label("Main Actors: " + movie.getActors());
        Label synopsisLabel = new Label("Synopsis: " + movie.getSynopsis());
        synopsisLabel.setWrapText(true);

        VBox buttons = new VBox(5);
        Button detailsButton = new Button("Movie Page");
        detailsButton.setOnAction(e -> {
            try {
                moviePage(movie);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        buttons.getChildren().add(detailsButton);

        // If Person is ContentManager, add an edit movie button.
        Person p = client.getConnectedPerson();
        if (p instanceof Worker && ((Worker) p).getWorkerType().equals("Content")) {
            Button editButton = new Button("Edit Movie");
            editButton.setOnAction(e -> {
                try {
                    editMoviePage(movie);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            buttons.getChildren().add(editButton);
        }

        textContent.getChildren().addAll(englishTitleLabel, hebrewTitleLabel, producerLabel, actorsLabel, synopsisLabel);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        HBox.setHgrow(buttons, Priority.NEVER);

        contentBox.getChildren().addAll(iv, textContent);
        HBox buttonBox = new HBox(buttons);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        contentBox.getChildren().add(buttonBox);

        movieBox.getChildren().add(contentBox);

        return movieBox;
    }

    private void moviePage(Movie movie) throws IOException {
        System.out.println("in moviePage");
        App.setRoot("CinemaMovieDetails", movie);
    }

    public void editMoviePage(Movie movie) throws IOException {
        System.out.println("in editMoviePage");
        App.setRoot("editMoviePage", movie);
    }

    @FXML
    private void handleBackButton(ActionEvent event) throws IOException {
        Person connectedPerson = client.getConnectedPerson();

        if (connectedPerson instanceof Customer) {
            App.setRoot("customerMenu", connectedPerson);
        } else if (connectedPerson instanceof Worker) {
            App.setRoot("UpdateContent", connectedPerson);
        } else {
            App.setRoot("Loginpage", null);
        }
    }


    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}