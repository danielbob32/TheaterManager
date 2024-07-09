package il.cshaifasweng.OCSFMediatorExample.client;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.client.events.*;

import java.io.IOException;
import java.util.List;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class CinemaMoviesListBoundary implements DataInitializable{
	private boolean isContentManager = false;



	@FXML
	private VBox moviesContainer;

	private SimpleClient client;

	public void initialize() {
		EventBus.getDefault().register(this);
	}

	public void setClient(SimpleClient client) {
		this.client = client;
	}

	@FXML
	private void showMovies() {
		System.out.println("in showMovies");
		// Clear previous content
		moviesContainer.getChildren().clear();
		// Request movies from the client
		client.getMovies();
	}

	@Override
	public void initData(Object data) {
		System.out.println("in initData with the next data" + data);
		if (data instanceof Boolean) {
			this.isContentManager = (Boolean) data;
			// You might want to update UI based on this value
			System.out.println("It's a content manager");
		}
	}

	@Subscribe
	public void onMovieListEvent(MovieListEvent event) {
		System.out.println("in onMovieListEvent");
		List<Movie> movies = event.getMovies();
		Platform.runLater(() -> {
			for (Movie movie : movies) {
				System.out.println("in onMovieListEvent for loop");
				// If the movie is now in theaters, add it to the list.
				if(movie.getIsCinema())
				{
					System.out.println("in onMovieListEvent for loop if");
					VBox movieBox = createMovieBox(movie);
					moviesContainer.getChildren().add(movieBox);
				}
			}
		});
	}

	private VBox createMovieBox(Movie movie) {
		System.out.println("in createMovieBox");
		VBox movieBox = new VBox(5);
		movieBox.setPadding(new Insets(10));
		movieBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5;");

		Label englishTitleLabel = new Label("English Title: " + movie.getEnglishName());
		Label hebrewTitleLabel = new Label("Hebrew Title: " + movie.getHebrewName());
		Label producerLabel = new Label("Producer: " + movie.getProducer());
		Label actorsLabel = new Label("Main Actors: " + movie.getActors());
		Label synopsisLabel = new Label("Synopsis " + movie.getSynopsis());

		Image image = new Image("@../resources/deadpool.jpg");
		ImageView iv = new ImageView();
		iv.setImage(image);
		movieBox.getChildren().addAll(englishTitleLabel, hebrewTitleLabel,producerLabel, actorsLabel, synopsisLabel, iv);
		return movieBox;
	}

	@FXML
	private void switchToSecondary() throws IOException {
		App.setRoot("secondary", false);
	}

	public void cleanup() {
		EventBus.getDefault().unregister(this);
	}

}
