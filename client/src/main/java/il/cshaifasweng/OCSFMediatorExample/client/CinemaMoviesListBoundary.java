package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.events.MovieListEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public class CinemaMoviesListBoundary implements DataInitializable {

	@FXML
	private VBox moviesContainer;

	@FXML
	private ComboBox<String> cinemaComboBox;

	@FXML
	private ComboBox<String> genreComboBox;

	@FXML
	private DatePicker fromDatePicker;

	@FXML
	private DatePicker toDatePicker;

	@FXML
	private Button filterButton;

	private SimpleClient client;
	private List<Movie> allMovies;

	public void initialize() {
		EventBus.getDefault().register(this);

		// Initialize ComboBoxes
		cinemaComboBox.getItems().addAll("Any", "Cinema City", "Yes Planet", "Lev HaMifratz", "Rav-Hen"); // Add your actual cinema names
		cinemaComboBox.setValue("Any");

		genreComboBox.getItems().addAll("Any", "Action", "Comedy", "Drama", "Fantasy", "Sci-Fi", "Family",
				"Children", "Romance", "Horror", "Adventure", "Animation", "Documentary"); // Add more genres as needed
		genreComboBox.setValue("Any");

		// Initialize DatePickers
		fromDatePicker.setValue(LocalDate.now());
		toDatePicker.setValue(LocalDate.now().plusWeeks(1));

		filterButton.setOnAction(event -> applyFilters());
//		showMovies();

	}

	@Override
	public void setClient(SimpleClient client) {
		this.client = client;
	}

	@FXML
	private void showMovies() {
		System.out.println("in showMovies");
		moviesContainer.getChildren().clear();
		client.getMovies();
	}

	@Override
	public void initData(Object data) {
		Person connectedPerson = client.getConnectedPerson();
		System.out.println("in initData with the next data" + data);
		showMovies();
	}

	@Subscribe
	public void onMovieListEvent(MovieListEvent event) {
		System.out.println("in onMovieListEvent");
		allMovies = event.getMovies();
//		applyFilters();
		displayFilteredMovies(allMovies);
	}

	private void applyFilters() {
		String selectedCinema = cinemaComboBox.getValue();
		String selectedGenre = genreComboBox.getValue();
		LocalDate fromDate = fromDatePicker.getValue();
		LocalDate toDate = toDatePicker.getValue();

		System.out.println("Applying filters:");
		System.out.println("Selected Cinema: " + selectedCinema);
		System.out.println("Selected Genre: " + selectedGenre);
		System.out.println("From Date: " + fromDate);
		System.out.println("To Date: " + toDate);
		System.out.println("Total movies before filtering: " + (allMovies != null ? allMovies.size() : 0));

		if (allMovies == null || allMovies.isEmpty()) {
			System.out.println("No movies to filter!");
			return;
		}

		List<Movie> filteredMovies = allMovies.stream()
				.filter(movie -> {
					boolean passes = movie.getIsCinema();
					System.out.println("Movie: " + movie.getEnglishName() + " - Is Cinema: " + passes);
					return passes;
				})
				.filter(movie -> {
					boolean passes = selectedCinema.equals("Any") || movie.getScreenings().stream()
							.anyMatch(screening -> screening.getCinema().getCinemaName().equals(selectedCinema));
					System.out.println("Movie: " + movie.getEnglishName() + " - Passes Cinema Filter: " + passes);
					return passes;
				})
				.filter(movie -> {
					boolean passes = selectedGenre.equals("Any") || movie.getGenres().contains(selectedGenre);
					System.out.println("Movie: " + movie.getEnglishName() + " - Passes Genre Filter: " + passes);
					return passes;
				})
				.filter(movie -> {
					List<Screening> screenings = movie.getScreenings();
					boolean passes = screenings.stream().anyMatch(screening -> {
						LocalDate screeningDate = screening.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						return !screeningDate.isBefore(fromDate) && !screeningDate.isAfter(toDate);
					});
					System.out.println("Movie: " + movie.getEnglishName() + " - Passes Date Filter: " + passes);
					return passes;
				})
				.collect(Collectors.toList());

		System.out.println("Total movies after filtering: " + filteredMovies.size());

		displayFilteredMovies(filteredMovies);
	}

	private void displayFilteredMovies(List<Movie> movies) {
		System.out.println("in displayFilteredMovies");
		Platform.runLater(() -> {
			moviesContainer.getChildren().clear();
			for (Movie movie : movies) {
				if(movie.getIsCinema()) {
					VBox movieBox = createMovieBox(movie);
					moviesContainer.getChildren().add(movieBox);
				}
			}
		});
	}

	private VBox createMovieBox(Movie movie) {
//		System.out.println("in createMovieBox");
		VBox movieBox = new VBox(5);

		movieBox.setPadding(new Insets(10));
		movieBox.setStyle("-fx-border-color: #1e3c72; -fx-border-width: 1; -fx-border-radius: 10; -fx-fill: #FFFFFFF2");

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
			System.out.println("Using default image");
			try {
				InputStream defaultImageStream = getClass().getClassLoader().getResourceAsStream("Images/default.jpg");
				if (defaultImageStream != null) {
					image3 = new Image(defaultImageStream);
					System.out.println("Default image loaded successfully");
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
		englishTitleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 17");
		Label hebrewTitleLabel = new Label(movie.getHebrewName());
		Label producerLabel = new Label("Producer: " + movie.getProducer());
		Label actorsLabel = new Label("Main Actors: " + movie.getActors());
		Label synopsisLabel = new Label("Synopsis: " + movie.getSynopsis());
		synopsisLabel.setWrapText(true);

		VBox buttons = new VBox(5);
		Button detailsButton = new Button("Movie Page");
		detailsButton.setStyle("-fx-background-color: #004e92; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 20");
		detailsButton.setCursor(Cursor.HAND);
		detailsButton.setOnAction(e-> {
            try {
                moviePage(movie);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

		buttons.getChildren().add(detailsButton);

		// If Person is ContentManager, add an edit movie button.
		Person p = client.getConnectedPerson();
//		if(p instanceof Worker && ((Worker) p).getWorkerType().equals("Content Manager"))
//		{
//			Button editButton = new Button("Edit Movie");
//			editButton.setOnAction(e-> {
//				try {
//					editMoviePage(movie);
//				} catch (IOException ex) {
//					throw new RuntimeException(ex);
//				}
//			});
//
//			buttons.getChildren().add(editButton);
//		}
		textContent.getChildren().addAll(englishTitleLabel, hebrewTitleLabel, producerLabel, actorsLabel, synopsisLabel);
		HBox.setHgrow(textContent, Priority.ALWAYS);
		HBox.setHgrow(buttons, Priority.NEVER);

		contentBox.getChildren().addAll(iv, textContent);
		HBox buttonBox = new HBox(buttons);
		buttonBox.setAlignment(Pos.CENTER_RIGHT);
		contentBox.getChildren().add(buttonBox);

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

	private void moviePage(Movie movie) throws IOException {
		System.out.println("in moviePage");
		Person connectedPerson = client.getConnectedPerson();
		cleanup();
		App.setRoot("CinemaMovieDetails", movie);
	}

//	public void editMoviePage(Movie movie) throws IOException {
//		System.out.println("in editMoviePage");
//		cleanup();
//		App.setRoot("editMoviePage", movie);
//	}

	@FXML
	private void handleBackButton(ActionEvent event) throws IOException {
		Person connectedPerson = client.getConnectedPerson();
		cleanup();
		if (connectedPerson instanceof Worker) {
			App.setRoot("UpdateContent", connectedPerson);
		} else if (connectedPerson instanceof Customer) {
			App.setRoot("customerMenu", connectedPerson);
		} else {
			App.setRoot("Loginpage", null);
		}
	}


	public void cleanup() {
		EventBus.getDefault().unregister(this);
	}
}