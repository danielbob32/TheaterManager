package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.events.MessageEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class CustomerMenuController implements DataInitializable {

    private SimpleClient client;

    @FXML
    private Label welcomeLabel;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private VBox buttonBox;

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        System.out.println("CustomerMenuController initialized");
        updateWelcomeMessage();
        addDynamicEffects();
    }

    private void updateWelcomeMessage() {
        Person connectedPerson = client.getConnectedPerson();
        if (connectedPerson != null) {
            welcomeLabel.setText("Welcome, " + connectedPerson.getName());
        }
    }

    private void addDynamicEffects() {
        // Adding a particle effect
        for (int i = 0; i < 100; i++) {
            javafx.scene.shape.Circle particle = new javafx.scene.shape.Circle(2, javafx.scene.paint.Color.web("rgba(255, 255, 255, 0.8)"));
            rootPane.getChildren().add(particle);

            double startX = Math.random() * rootPane.getWidth();
            double startY = Math.random() * rootPane.getHeight();

            particle.setTranslateX(startX);
            particle.setTranslateY(startY);

            javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(Duration.seconds(4), particle);
            fadeTransition.setFromValue(1);
            fadeTransition.setToValue(0);
            fadeTransition.setCycleCount(javafx.animation.FadeTransition.INDEFINITE);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
        }
    }

    @FXML
    private void viewFutureMovies() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        App.setRoot("FutureMoviesPage", connectedPerson);
    }

    @FXML
    private void viewHomeMovies() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        System.out.println("Going to HomeMovieList");
        App.setRoot("HomeMovieList", connectedPerson);
    }

    @FXML
    private void viewMovieList() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        App.setRoot("CinemaMovieList", connectedPerson);
    }

    @Subscribe
    public void onNewMovieNotification(MessageEvent event) {
        if (event.getMessage().getMessage().equals("newMovieNotification")) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("New Movie Added");
                alert.setHeaderText(null);
                alert.setContentText(event.getMessage().getData());
                alert.show();
            });
        }
    }

    @FXML
    private void buyTicketTab() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        App.setRoot("PurchaseTicketTab", connectedPerson);
    }

    @FXML
    private void refundTickets() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        App.setRoot("BookingsBoundary", connectedPerson);
    }

    @FXML
    private void fileComplaint() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        App.setRoot("CustomerComplaint", null);
    }

    @FXML
    private void handleLogout() throws IOException {
        client.logout();
        App.setRoot("LoginPage", null);
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}
