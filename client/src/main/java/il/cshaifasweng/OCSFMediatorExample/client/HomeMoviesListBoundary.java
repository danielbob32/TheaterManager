package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.events.MovieListEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
        System.out.println("in showHomeMovies here daniel");
        showHomeMovies();
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
        //System.out.println("in createMovieBox");
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
        Label englishTitleLabel = new Label(movie.getEnglishName());
        englishTitleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15;");
        Label hebrewTitleLabel = new Label(movie.getHebrewName());
        Label producerLabel = new Label("Producer: " + movie.getProducer());
        Label actorsLabel = new Label("Main Actors: " + movie.getActors());
        Label synopsisLabel = new Label("Synopsis: " + movie.getSynopsis());
        synopsisLabel.setWrapText(true);

        VBox buttons = new VBox(5);
        Button detailsButton = new Button("Movie Page");
        detailsButton.setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15;");
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
        if(p!=null && p instanceof Worker)
        {
            if ((((Worker) p).getWorkerType().equals("Content manager"))||
                    (((Worker) p).getWorkerType().equals("Chain manager"))) {
                Button editButton = new Button("Delete Movie");
                editButton.setOnAction(e -> {
                    try {
                        deleteMovie(movie);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

                buttons.getChildren().add(editButton);
            }
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
        App.setRoot("HomeMovieDetails", movie);
    }

    @FXML
    private void deleteMovie(Movie movie) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Movie");
        alert.setHeaderText("Are you sure you want to delete this movie?");
        alert.setContentText("This action will delete the movie and all its screenings from all cinemas.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    client.sendToServer(new Message(0, "deleteMovie:home", String.valueOf(movie.getId())));
                    Person connectedPerson = client.getConnectedPerson();
                    App.setRoot("HomeMovieList", connectedPerson);
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to delete movie. Please try again.");
                }
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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