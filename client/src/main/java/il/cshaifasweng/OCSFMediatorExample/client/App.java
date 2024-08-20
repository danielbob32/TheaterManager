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
import java.util.function.Consumer;

import static il.cshaifasweng.OCSFMediatorExample.client.WorkerMenuController.workerType;

public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;
    private static SimpleClient client;

    @Override
    public void start(Stage stage) throws IOException {
        EventBus.getDefault().register(this);
        client = SimpleClient.getClient();
        client.openConnection();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Loginpage.fxml"));
        Parent root = loader.load();
        Object controller = loader.getController();
        ((DataInitializable)controller).setClient(client);
        ((DataInitializable)controller).initData(null);
//        scene = new Scene(root, 1000, 500);

        // Get screen dimensions
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        // Set scene dimensions to full height and half width
        scene = new Scene(root, screenWidth - 500, screenHeight - 20);
        scene.getStylesheets().add(getClass().getResource("App.css").toExternalForm());
        primaryStage = stage;
        stage.setScene(scene);
        stage.show();
        stage.setMaxWidth(1080);
        stage.setMaxHeight(900);
        //stage.setResizable(false);
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }


    public static void setRoot(String fxml,Object controllerData) throws IOException  {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        scene.setRoot(loader.load());
        Object controller = loader.getController();
        if (controller instanceof DataInitializable) {
            ((DataInitializable)controller).setClient(client);
            ((DataInitializable) controller).initData(controllerData);
        }
    }


    public static void setRoot2(String fxml, Consumer<Object> controllerConsumer) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent root = fxmlLoader.load();
        Object controller = fxmlLoader.getController();
        if (controllerConsumer != null) {
            controllerConsumer.accept(controller);
        }
        if (controller instanceof WorkerMenuController) {
            ((WorkerMenuController) controller).setWorkerType(workerType);
        }
        scene.setRoot(root);
    }


    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
            Object controller = fxmlLoader.getController();
            if (controller instanceof WorkerMenuController) {
                ((WorkerMenuController) controller).setWorkerType(workerType);
            }
            System.out.println("Successfully loaded FXML: " + fxml);
        } catch (IOException e) {
            System.err.println("Failed to load FXML file: " + fxml);
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return root;
    }
    public static void setWorkerType(String type) {
        workerType = type;
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