package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;
    private SimpleClient client;

    @Override
    public void start(Stage stage) throws IOException {
        EventBus.getDefault().register(this);
        client = SimpleClient.getClient();
        client.openConnection();
        scene = new Scene(loadFXML("Loginpage"), 640, 480);
        primaryStage = stage;
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    @Override
    public void stop() throws Exception {
        EventBus.getDefault().unregister(this);
        super.stop();
    }

    @Subscribe
    public void onWarningEvent(WarningEvent event) {
        Platform.runLater(() -> {
            String message = event.getWarning().getMessage();
            if (message.contains("Customer login successful")) {
                try {
                    setRoot("CustomerMenu");
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Could not load customer menu.");
                }
            } else if (message.contains("Worker login successful")) {
                try {
                    setRoot("WorkerMenu");
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Could not load worker menu.");
                }
            } else {
                showAlert("Warning", String.format("Message: %s\nTimestamp: %s\n",
                        message,
                        event.getWarning().getTime().toString()));
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    public static void main(String[] args) {
        launch();
    }
}