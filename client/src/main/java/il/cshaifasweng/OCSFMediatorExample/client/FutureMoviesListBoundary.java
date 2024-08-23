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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the Future Movies List scene. Manages initialization, movie display, and scene navigation.
 */
public class FutureMoviesListBoundary implements DataInitializable {

    @FXML
    private VBox moviesContainer;

    private SimpleClient client;
    private List<Movie> allMovies;

    /**
     * Initializes the controller by registering with the EventBus.
     */
    public void initialize() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    /**
     * Requests and displays the list of future movies when called.
     */
    @FXML
    private void showFutureMovies() {
        moviesContainer.getChildren().clear();
        client.getMovies();
    }

    @Override
    public void initData(Object data) {
        moviesContainer.getChildren().clear();
        client.getMovies();
    }

    /**
     * Handles the MovieListEvent by updating the list of movies and displaying future ones.
     *
     * @param event The event containing the list of movies.
     */
    @Subscribe
    public void onMovieListEvent(MovieListEvent event) {
        allMovies = event.getMovies();
        displayFutureMovies();
    }

    /**
     * Filters and displays movies with a premiere date in the future.
     */
    private void displayFutureMovies() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_YEAR, 10);
        Date futureDate = calendar.getTime();

        List<Movie> futureMovies = allMovies.stream()
                .filter(movie -> movie.getPremier().after(futureDate)
                        && !movie.getIsHome()
                        && !movie.getIsCinema())
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            moviesContainer.getChildren().clear();
            for (Movie movie : futureMovies) {
                VBox movieBox = createMovieBox(movie);
                moviesContainer.getChildren().add(movieBox);
            }
        });
    }

    /**
     * Creates a VBox representing a movie, including its image and details.
     *
     * @param movie The movie to display.
     * @return A VBox containing the movie's details.
     */
    private VBox createMovieBox(Movie movie) {
        VBox movieBox = new VBox(5);
        movieBox.setPadding(new Insets(10));
        movieBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5;");

        HBox contentBox = new HBox(10);
        contentBox.prefWidthProperty().bind(this.moviesContainer.widthProperty().multiply(0.80));

        Image image = convertByteArrayToImage(movie.getMovieIcon());
        if (image == null || image.isError()) {
            try {
                InputStream defaultImageStream = getClass().getClassLoader().getResourceAsStream("Images/default.jpg");
                if (defaultImageStream != null) {
                    image = new Image(defaultImageStream);
                }
            } catch (Exception e) {
                System.out.println("Error loading default image: " + e.getMessage());
            }
        }
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

        textContent.getChildren().addAll(englishTitleLabel, hebrewTitleLabel, producerLabel, actorsLabel, synopsisLabel);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        contentBox.getChildren().addAll(iv, textContent);
        movieBox.getChildren().add(contentBox);

        return movieBox;
    }

    /**
     * Converts a byte array to an Image.
     *
     * @param imageData The byte array representing the image.
     * @return The Image object, or null if no data is available.
     */
    public Image convertByteArrayToImage(byte[] imageData) {
        if (imageData != null && imageData.length > 0) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            return new Image(inputStream);
        }
        return null;
    }

    /**
     * Handles the back button action, navigating to the appropriate scene based on the user type.
     *
     * @param event The ActionEvent triggered by the back button.
     * @throws IOException If an error occurs while loading the scene.
     */
    @FXML
    private void handleBackButton(ActionEvent event) throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
        if (connectedPerson instanceof Customer) {
            App.setRoot("customerMenu", connectedPerson);
        } else if (connectedPerson instanceof Worker) {
            App.setRoot("UpdateContent", connectedPerson);
        } else {
            App.setRoot("LoginPage", null);
        }
    }

    /**
     * Cleans up resources and unregisters from the EventBus.
     */
    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}
