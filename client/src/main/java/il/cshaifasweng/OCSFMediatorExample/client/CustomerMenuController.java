package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.events.MessageEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.NotificationEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Notification;
import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;

public class CustomerMenuController implements DataInitializable {

    private SimpleClient client;

    @FXML
    private Label welcomeLabel;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private VBox buttonBox;

    @FXML
    private VBox notificationsBoard;

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        System.out.println("CustomerMenuController initialized");
        updateWelcomeMessage();
        EventBus.getDefault().register(this);
        displayNotifications();
        //addDynamicEffects();
    }

    private void updateWelcomeMessage() {
        Person connectedPerson = client.getConnectedPerson();
        if (connectedPerson != null) {
            welcomeLabel.setText("Welcome, " + connectedPerson.getName());
        }
    }

//    private void addDynamicEffects() {
//        // Adding a particle effect
//        for (int i = 0; i < 100; i++) {
//            javafx.scene.shape.Circle particle = new javafx.scene.shape.Circle(2, javafx.scene.paint.Color.web("rgba(255, 255, 255, 0.8)"));
//            rootPane.getChildren().add(particle);
//
//            double startX = Math.random() * rootPane.getWidth();
//            double startY = Math.random() * rootPane.getHeight();
//
//            particle.setTranslateX(startX);
//            particle.setTranslateY(startY);
//
//            javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(Duration.seconds(4), particle);
//            fadeTransition.setFromValue(1);
//            fadeTransition.setToValue(0);
//            fadeTransition.setCycleCount(javafx.animation.FadeTransition.INDEFINITE);
//            fadeTransition.setAutoReverse(true);
//            fadeTransition.play();
//        }
//    }

    private void requestNotifications() {
        try {
            client.requestNotifications();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onNotificationEvent(NotificationEvent event) {
        Platform.runLater(() -> updateNotificationsDisplay(event.getNotifications()));
    }

    @FXML
    public void displayNotifications() {
        try {
            client.requestNotifications();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to request notifications: " + e.getMessage());
        }
    }

    private void updateNotificationsDisplay(List<Notification> notifications) {
        notificationsBoard.getChildren().clear();
        if (notifications.isEmpty()) {
            Label noNotificationsLabel = new Label("No new notifications");
            noNotificationsLabel.getStyleClass().add("notification");
            notificationsBoard.getChildren().add(noNotificationsLabel);
        } else {
            for (Notification notification : notifications) {
                HBox notificationBox = new HBox(10);
                notificationBox.getStyleClass().add("notification");

                Label notificationLabel = new Label(notification.getMessage());
                Button markReadButton = new Button("Mark as Read");
                markReadButton.setOnAction(e -> markNotificationAsRead(notification));

                notificationBox.getChildren().addAll(notificationLabel, markReadButton);
                notificationsBoard.getChildren().add(notificationBox);
            }
        }
    }

    private void markNotificationAsRead(Notification notification) {
        try {
            client.markNotificationAsRead(notification.getId());
            notificationsBoard.getChildren().removeIf(node ->
                    node instanceof HBox && ((HBox) node).getChildren().get(0) instanceof Label &&
                            ((Label) ((HBox) node).getChildren().get(0)).getText().equals(notification.getMessage())
            );
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to mark notification as read: " + e.getMessage());
        }
    }

    @FXML
    private void viewFutureMovies() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
        App.setRoot("FutureMoviesPage", connectedPerson);
    }

    @FXML
    private void viewHomeMovies() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        System.out.println("Going to HomeMovieList");
        cleanup();
        App.setRoot("HomeMovieList", connectedPerson);
    }

    @FXML
    private void viewMovieList() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
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
        cleanup();
        App.setRoot("PurchaseTicketTab", connectedPerson);
    }

    @FXML
    private void refundTickets() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        cleanup();
        App.setRoot("BookingsBoundary", connectedPerson);
    }

    @FXML
    private void fileComplaint() throws IOException {
        cleanup();
        App.setRoot("CustomerComplaint", null);
    }

    @FXML
    private void handleLogout() throws IOException {
        client.logout();
        cleanup();
        App.setRoot("LoginPage", null);
    }

    @FXML
    private void handleRefreshNotifications() {
        displayNotifications();
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }
}
