package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
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

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    
    

    @Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
    	EventBus.getDefault().unregister(this);
		super.stop();
	}
    
    @Subscribe
    public void onWarningEvent(WarningEvent event) {
    	Platform.runLater(() -> {
    		Alert alert = new Alert(AlertType.WARNING,
        			String.format("Message: %s\nTimestamp: %s\n",
        					event.getWarning().getMessage(),
        					event.getWarning().getTime().toString())
        	);
        	alert.show();
    	});
    	
    }

	public static void main(String[] args) {
        launch();
    }

}