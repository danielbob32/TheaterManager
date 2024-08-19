package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.events.MovieListEvent;
//import il.cshaifasweng.OCSFMediatorExample.client.events.MovieListUpdatedEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.Movie;
import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class LoginPageController implements Initializable, DataInitializable {



    private SimpleClient client;

    @FXML
    private ComboBox<String> userTypeComboBox;

    @FXML
    private TextField idTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Button loginButton;

    @FXML
    private TextField timeField;

    @FXML
    private Circle clockBackground;

    @FXML
    private  ImageView moviePoster;

    @FXML
    private Button comingSoonBtn;
    @FXML
    private Button homeMoviesBtn;
    @FXML
    private Button inTheatersBtn;
    @FXML
    private Button buyTicketBtn;

    @FXML
    private AnchorPane mainAnchorPane;

    private List<Movie> allMovies;

    private Timeline slideshowTimeline;


    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        System.out.println("LoginPageController initialized");
        System.out.println("Requesting movies from server");
        if (client == null) {
            client = SimpleClient.getClient();
        }
        client.getMovies();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        EventBus.getDefault().register(this);
        System.out.println("Requesting movies from server");
        System.out.println("LoginPageController initialized");
        userTypeComboBox.getItems().addAll("Worker", "Customer");
        userTypeComboBox.setOnAction(this::handleUserTypeSelection);
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);
        setupClock();
        animateClock();
        addButtonHoverEffects();
        addParticleEffect();
//        if (client == null) {
//            client = SimpleClient.getClient();
//        }
//        client.getMovies();
    }

    @Subscribe
    public void onMovieListEvent(MovieListEvent event) {
        System.out.println("Received MovieListEvent");
        allMovies = event.getMovies();
        filterMovies(allMovies);
        stopSlideshow();
        startMoviePosterSlideshow();
    }

    // @Subscribe
    // public void onMovieListUpdatedEvent(MovieListUpdatedEvent event) {
    //     allMovies = event.getUpdatedMovies();
    //     filterMovies(allMovies);
    //     stopSlideshow();
    //     //startMoviePosterSlideshow();
    // }

    private void stopSlideshow() {
        if (slideshowTimeline != null) {
            slideshowTimeline.stop();
        }
    }


    @FXML
    void handleUserTypeSelection(ActionEvent event) {
        String selectedUserType = userTypeComboBox.getValue();
        boolean isWorker = "Worker".equals(selectedUserType);
        passwordTextField.setVisible(isWorker);
        passwordTextField.setManaged(isWorker);
    }

    @FXML
    void handleLogin(ActionEvent event) {
        System.out.println("Login button pressed");
        String userType = userTypeComboBox.getValue();
        String idText = idTextField.getText();
        String password = passwordTextField.getText();

        if (userType == null || userType.isEmpty()) {
            showAlert("Error", "Please select a user type.");
            return;
        }

        if (idText.isEmpty()) {
            showAlert("Error", "Please enter an ID.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showAlert("Error", "ID must be a number.");
            return;
        }

        if ("Worker".equals(userType) && password.isEmpty()) {
            showAlert("Error", "Please enter a password for worker login.");
            return;
        }

        System.out.println("Sending login request to server");
        if ("Worker".equals(userType)) {
            Worker worker = new Worker("Yaren", password, id);
            cleanup();
            stopSlideshow();
            client.tryWorkerLogin(worker);
    //                SimpleClient.getClient().sendToServer(worker);
        } else {
            Customer customer = new Customer("Yarden", "yarden@gmail.com", id);
            cleanup();
            stopSlideshow();
            client.tryCustomerLogin(customer);
//            SimpleClient.getClient().sendToServer(customer);
        }
    }

    @FXML
    private void viewFutureMovies() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        stopSlideshow();
        cleanup();
        App.setRoot("FutureMoviesPage", null);
    }

    @FXML
    private void viewHomeMovies() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        stopSlideshow();
        cleanup();
        App.setRoot("HomeMovieList", null);
    }

    @FXML
    private void viewMovieList() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        stopSlideshow();
        cleanup();
        App.setRoot("CinemaMovieList", null);
    }


    @FXML
    void buyTicketTab(ActionEvent event) throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        stopSlideshow();
        cleanup();
        App.setRoot("purchaseTicketTab", null);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void animateClock() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(clockBackground.scaleXProperty(), 1)),
                new KeyFrame(Duration.ZERO, new KeyValue(clockBackground.scaleYProperty(), 1)),
                new KeyFrame(Duration.seconds(0.5), new KeyValue(clockBackground.scaleXProperty(), 1.1)),
                new KeyFrame(Duration.seconds(0.5), new KeyValue(clockBackground.scaleYProperty(), 1.1)),
                new KeyFrame(Duration.seconds(1), new KeyValue(clockBackground.scaleXProperty(), 1)),
                new KeyFrame(Duration.seconds(1), new KeyValue(clockBackground.scaleYProperty(), 1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void filterMovies(List<Movie> movies) {
        // Iterate through the list of movies
        movies.removeIf(movie -> !movie.getIsHome() && !movie.getIsCinema());
    }


    private void startMoviePosterSlideshow() {

        if (allMovies == null || allMovies.isEmpty()) {
            System.out.println("No movies available for slideshow");
            return;
        }

        System.out.println("Number of movies received: " + allMovies.size());

        Timeline timeline = new Timeline();
        for (int i = 0; i < allMovies.size(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(i * 5), event -> {
                Movie movie = allMovies.get(index);
                byte[] image = movie.getMovieIcon();

                // Convert byte array to Image
                Image image2 = convertByteArrayToImage(image);
                // Handle null or error image
                if (image2 == null || image2.isError()) {
                    System.out.println("Using default image");
                    try {
                        InputStream defaultImageStream = getClass().getClassLoader().getResourceAsStream("Images/default.jpg");
                        if (defaultImageStream != null) {
                            image2 = new Image(defaultImageStream);
                            System.out.println("Default image loaded successfully");
                        } else {
                            System.out.println("Default image not found");
                        }
                    } catch (Exception e) {
                        System.out.println("Error loading default image: " + e.getMessage());
                    }
                }

                // Apply fade transition for the image
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), moviePoster);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                //System.out.println("Fading out image: " + index + "movie title:" + movie.getEnglishName());
                // Set the image after fading out
                Image finalImage = image2;
                fadeOut.setOnFinished(e -> {
                    moviePoster.setImage(finalImage);
                    if (finalImage == null) {
                        System.out.println("Final image is null");
                    } else {
                        //System.out.println("Setting image to ImageView");
                    }
                    // Fade in the new image
                    FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), moviePoster);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });

                fadeOut.play();
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.setCycleCount(Timeline.INDEFINITE);
        slideshowTimeline = timeline;
        slideshowTimeline.play();    }


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

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

    private void addButtonHoverEffects() {
        addHoverEffect(comingSoonBtn);
        addHoverEffect(homeMoviesBtn);
        addHoverEffect(inTheatersBtn);
        addHoverEffect(buyTicketBtn);
    }

    private void addHoverEffect(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.1);
            st.setToY(1.1);
            st.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });
    }

    private void addParticleEffect() {
        for (int i = 0; i < 50; i++) {
            Circle particle = new Circle(2, Color.WHITE);
            particle.setOpacity(0.7);
            mainAnchorPane.getChildren().add(particle);

            double startX = Math.random() * mainAnchorPane.getWidth();
            double startY = Math.random() * mainAnchorPane.getHeight();

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(particle.translateXProperty(), startX),
                            new KeyValue(particle.translateYProperty(), startY)
                    ),
                    new KeyFrame(Duration.seconds(10 + Math.random() * 20),
                            new KeyValue(particle.translateXProperty(), Math.random() * mainAnchorPane.getWidth()),
                            new KeyValue(particle.translateYProperty(), Math.random() * mainAnchorPane.getHeight())
                    )
            );
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
    }

    private void setupClock() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalTime currentTime = LocalTime.now();
            timeField.setText(currentTime.format(dtf));
        }),
                new KeyFrame(Duration.seconds(1))
        );
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
}