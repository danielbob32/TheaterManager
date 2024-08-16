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
import javafx.scene.control.Button;
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

public class FutureMoviesListBoundary implements DataInitializable {

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
    private void showFutureMovies() {
//        System.out.println("in showFutureMovies");
        moviesContainer.getChildren().clear();
        client.getMovies();
    }

    @Override
    public void initData(Object data) {
//        System.out.println("in initData with the next data" + data);
        moviesContainer.getChildren().clear();
        client.getMovies();
    }

    @Subscribe
    public void onMovieListEvent(MovieListEvent event) {
//        System.out.println("in onMovieListEvent");
        allMovies = event.getMovies();
        displayFutureMovies();
    }

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

    private VBox createMovieBox(Movie movie) {
//        System.out.println("in createMovieBox");
        VBox movieBox = new VBox(5);

        movieBox.setPadding(new Insets(10));
        movieBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5;");

        HBox contentBox = new HBox(10);
        contentBox.prefWidthProperty().bind(this.moviesContainer.widthProperty().multiply(0.80));

        byte[] image2 = movie.getMovieIcon();
        if (image2 != null) {
//			System.out.println("Image byte array length: " + image2.length);
        } else {
            System.out.println("Image byte array is null");
        }
        Image image3 = convertByteArrayToImage(image2);
        if (image3 == null) {
            System.out.println("Image is null");
        } else {
//			System.out.println("Image created successfully");
        }
        if (image3 == null || image3.isError()) {
//            System.out.println("Using default image");
            try {
                InputStream defaultImageStream = getClass().getClassLoader().getResourceAsStream("Images/default.jpg");
                if (defaultImageStream != null) {
                    image3 = new Image(defaultImageStream);
//					System.out.println("Default image loaded successfully");
                } else {
					System.out.println("Default image not found");
                }
            } catch (Exception e) {
                System.out.println("Error loading default image: " + e.getMessage());
            }
        }
        ImageView iv = new ImageView(image3);
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
        // If Person is ContentManager, add an edit movie button.
        Person p = client.getConnectedPerson();
        if (p instanceof Worker && ((Worker) p).getWorkerType().equals("Content")) {
            VBox buttonBox = new VBox(5);
            Button editButton = new Button("Edit Movie");
            editButton.setOnAction(e -> {
                try {
                    editMoviePage(movie);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            buttonBox.getChildren().add(editButton);
            HBox.setHgrow(buttonBox, Priority.NEVER);
            contentBox.getChildren().add(buttonBox);
        }
        movieBox.getChildren().add(contentBox);

        return movieBox;
    }

    public Image convertByteArrayToImage(byte[] imageData) {
        if (imageData != null && imageData.length > 0) {
            // Convert byte[] to InputStream
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);

            // Create Image from InputStream
            return new Image(inputStream);
        } else {
            System.out.println("No image data available");
            return null;  // Or handle as needed, e.g., return a default image
        }
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


    private void editMoviePage(Movie movie) throws IOException {
        System.out.println("in editMoviePage");
        App.setRoot("editMoviePage", movie);
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}