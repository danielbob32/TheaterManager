package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Customer;
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

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private ImageView moviePoster;

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

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        System.out.println("CustomerMenuController initialized");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("LoginPageController initialized");
        userTypeComboBox.getItems().addAll("Worker", "Customer");
        userTypeComboBox.setOnAction(this::handleUserTypeSelection);
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);
        setupClock();
        animateClock();
        startMoviePosterSlideshow();
        addButtonHoverEffects();
        addParticleEffect();
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
            client.tryWorkerLogin(worker);
    //                SimpleClient.getClient().sendToServer(worker);
        } else {
            Customer customer = new Customer("Yarden", "yarden@gmail.com", id);
            client.tryCustomerLogin(customer);
//            SimpleClient.getClient().sendToServer(customer);
        }
    }

    @FXML
    private void viewFutureMovies() throws IOException {
        Person connectedPerson = client.getConnectedPerson();

        App.setRoot("FutureMoviesPage", null);
    }

    @FXML
    private void viewHomeMovies() throws IOException {
        Person connectedPerson = client.getConnectedPerson();

        App.setRoot("HomeMovieList", null);
    }

    @FXML
    private void viewMovieList() throws IOException {
        Person connectedPerson = client.getConnectedPerson();

        App.setRoot("CinemaMovieList", null);
    }


    @FXML
    void buyTicketTab(ActionEvent event) throws IOException {
        Person connectedPerson = client.getConnectedPerson();

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

    private void startMoviePosterSlideshow() {
        String[] posterUrls = {
                "./Images/deadpool.jpg",
                "./Images/smurfs.jpg",
                "./Images/default.jpg"
        };

        Timeline timeline = new Timeline();
        for (int i = 0; i < posterUrls.length; i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(i * 5), event -> {
                Image image = new Image(posterUrls[index]);
                FadeTransition ft = new FadeTransition(Duration.seconds(1), moviePoster);
                ft.setFromValue(1.0);
                ft.setToValue(0.0);
                ft.setOnFinished(e -> {
                    moviePoster.setImage(image);
                    FadeTransition ft2 = new FadeTransition(Duration.seconds(1), moviePoster);
                    ft2.setFromValue(0.0);
                    ft2.setToValue(1.0);
                    ft2.play();
                });
                ft.play();
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
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