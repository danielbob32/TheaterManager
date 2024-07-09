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

public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;
    private SimpleClient client;

    @Override
    public void start(Stage stage) throws IOException {

    	EventBus.getDefault().register(this);
    	client = SimpleClient.getClient();
    	client.openConnection();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CinemaMovieList.fxml"));
        Parent root = loader.load();
        CinemaMoviesListBoundary controller = loader.getController();
        controller.setClient(client);

        // Set isContentManager value
        boolean isContentManager = true; // or false, depending on your logic
        controller.initData(isContentManager);

        scene = new Scene(root, 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml,Object controllerData) throws IOException  {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        scene.setRoot(loader.load());
        Object controller = loader.getController();
        if (controller instanceof DataInitializable) {
            ((DataInitializable) controller).initData(controllerData);
        }
    }

    static void setRoot(String fxml, Consumer<Object> controllerConsumer) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent root = fxmlLoader.load();
        if (controllerConsumer != null) {
            controllerConsumer.accept(fxmlLoader.getController());
        }
        scene.setRoot(root);
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