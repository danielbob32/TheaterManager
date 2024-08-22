package il.cshaifasweng.OCSFMediatorExample.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import il.cshaifasweng.OCSFMediatorExample.client.events.CinemaListEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.MessageEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.MovieDeleteEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.MovieEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class CinemaMovieDetailsBoundary implements DataInitializable {

    @FXML private Button backButton;
    @FXML private Button deleteMovieButton;
    @FXML private Button addScreeningButton;
    @FXML private Button deleteScreeningButton;
    @FXML private ImageView movieImage;
    @FXML private Label englishTitleLabel;
    @FXML private Label hebrewTitleLabel;
    @FXML private Label producerLabel;
    @FXML private Label actorsLabel;
    @FXML private Label durationLabel;
    @FXML private Label genreLabel;
    @FXML private TextArea synopsisArea;
    @FXML private ComboBox<Cinema> cinemaComboBox;
    @FXML private ListView<Screening> screeningListView;
    @FXML private Button buyTicketsButton;

    private SimpleClient client;
    private boolean isContentManager = false;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Movie currentMovie;
    public void initialize() {
        EventBus.getDefault().register(this);
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        if (data instanceof Movie) {
            currentMovie = (Movie) data;
            client.getMovieById(currentMovie.getId());
        }
    }

    @Subscribe
    public void onMovieEvent(MovieEvent event) {
        if(event.getSuccess())
        {
            currentMovie = event.getMovie();
            System.out.println("CinemaMovieDetails: Got Current Movie:" + currentMovie.getEnglishName());
            Platform.runLater(() -> {
                displayMovieDetails();
//                populateCinemas();
                client.requestCinemaList();
                checkUserPermissions();
            });
        }
        else {
            client.showErrorAlert("Oops! Couldn't find the asked movie!");
        }
    }

    private void checkUserPermissions() {
        Person connectedPerson = client.getConnectedPerson();
        isContentManager = (connectedPerson instanceof Worker) &&
                (((Worker) connectedPerson).getWorkerType().equals("Content manager") ||
                ((Worker) connectedPerson).getWorkerType().equals("Cinema manager") ||
                ((Worker) connectedPerson).getWorkerType().equals("Chain manager"));

        deleteMovieButton.setVisible(isContentManager);
        addScreeningButton.setVisible(isContentManager);
        deleteScreeningButton.setVisible(isContentManager);
        buyTicketsButton.setVisible(!isContentManager);
    }

    private void displayMovieDetails() {
        englishTitleLabel.setText(currentMovie.getEnglishName());
        hebrewTitleLabel.setText(currentMovie.getHebrewName());
        producerLabel.setText("Producer: " + currentMovie.getProducer());
        actorsLabel.setText("Actors: " + currentMovie.getActors());
        durationLabel.setText("Duration: " + currentMovie.getDuration() + " minutes");
        genreLabel.setText("Genres: " + String.join(", ", currentMovie.getGenres()));
        synopsisArea.setText(currentMovie.getSynopsis());

        byte[] image2 = currentMovie.getMovieIcon();
        if (image2 != null) {
            System.out.println("Image byte array length: " + image2.length);
        } else {
            System.out.println("Image byte array is null");
        }
        Image image3 = convertByteArrayToImage(image2);
        if (image3 == null) {
            System.out.println("Image is null");
        } else {
            System.out.println("Image created successfully");
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
        movieImage.setImage(image3);
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

//    private void populateCinemas() {
//        List<Cinema> cinemas = currentMovie.getScreenings().stream()
//                .map(Screening::getCinema)
//                .distinct()
//                .collect(Collectors.toList());
//        cinemaComboBox.setItems(FXCollections.observableArrayList(cinemas));
//
//        cinemaComboBox.setConverter(new StringConverter<Cinema>() {
//            @Override
//            public String toString(Cinema cinema) {
//                return cinema != null ? cinema.getCinemaName() : "";
//            }
//
//            @Override
//            public Cinema fromString(String string) {
//                return cinemaComboBox.getItems().stream()
//                        .filter(cinema -> cinema.getCinemaName().equals(string))
//                        .findFirst()
//                        .orElse(null);
//            }
//        });
//    }
    @Subscribe
    public void onCinemaListEvent(CinemaListEvent event) {
        List<Cinema> cinemas = event.getCinemas();
        cinemaComboBox.getItems().clear();
        cinemaComboBox.setItems(FXCollections.observableArrayList(cinemas));

        cinemaComboBox.setConverter(new StringConverter<Cinema>() {
            @Override
            public String toString(Cinema cinema) {
                return cinema != null ? cinema.getCinemaName() : "";
            }

            @Override
            public Cinema fromString(String string) {
                return cinemaComboBox.getItems().stream()
                        .filter(cinema -> cinema.getCinemaName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    @FXML
    private void handleCinemaSelection() {
        Cinema selectedCinema = cinemaComboBox.getSelectionModel().getSelectedItem();
        if (selectedCinema != null) {
            System.out.println("CinemaMovieDetailsBoundary: handleCinemaSelection: in if");
            List<Screening> screenings = currentMovie.getScreenings().stream()
                    .filter(s -> s.getCinema().getCinemaName().equals(selectedCinema.getCinemaName()))
                    .collect(Collectors.toList());
            System.out.println("CinemaMovieDetailsBoundary: handleCinemaSelection: screenings size:" + screenings.size());
            ObservableList<Screening> screeningItems = FXCollections.observableArrayList(screenings);
            screeningListView.setItems(screeningItems);

            screeningListView.setCellFactory(lv -> new ListCell<Screening>() {
                @Override
                protected void updateItem(Screening screening, boolean empty) {
                    super.updateItem(screening, empty);
                    if (empty || screening == null) {
                        setText(null);
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        setText(sdf.format(screening.getTime()) + " - Hall: " + screening.getHall().getHallNumber());
                    }
                }
            });
        }
    }



    @FXML
    private void handleBackButton() throws IOException {
        cleanup();
        App.setRoot("CinemaMovieList", null);
    }

    @FXML
    private void handleBuyTickets() throws IOException {
        Screening selectedScreening = screeningListView.getSelectionModel().getSelectedItem();
        if (selectedScreening != null) {
            selectedScreening.setMovie(currentMovie);
            cleanup();
            App.setRoot("PurchaseTickets", selectedScreening);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Screening Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a screening before proceeding to purchase tickets.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDeleteMovie() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Movie");
        alert.setHeaderText("Are you sure you want to delete this movie?");
        alert.setContentText("This action will delete the movie and all its screenings from all cinemas.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    client.sendToServer(new Message(0, "deleteMovie:cinema", String.valueOf(currentMovie.getId())));
                    Person connectedPerson = client.getConnectedPerson();
//                    cleanup();
//                    App.setRoot("CinemaMovieList", connectedPerson);
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to delete movie. Please try again.");
                }
            }
        });
    }


    @FXML
    private void handleDeleteScreening() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Screening");
        alert.setHeaderText("Are you sure you want to delete this screening?");
        alert.setContentText("This action will delete the screening and all its tickets.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Screening selectedScreening = screeningListView.getSelectionModel().getSelectedItem();
                if (selectedScreening == null) {
                    showAlert("Error", "Please select a screening to delete.");
                    return;
                }
                try {
                    currentMovie.removeScreening(selectedScreening);
                    client.sendToServer(new Message(0, "deleteScreening", String.valueOf(selectedScreening.getScreening_id())));
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to delete screening. Please try again.");
                }
            }
        });
    }

    private void setupDatePicker(DatePicker datePicker) {
        LocalDate today = LocalDate.now();
        datePicker.setValue(today);
        datePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });
    }

    @FXML
    private void handleAddScreening() {
        Cinema selectedCinema = cinemaComboBox.getSelectionModel().getSelectedItem();
        if (selectedCinema == null) {
            showAlert("Error", "Please select a cinema first.");
            return;
        }

        Dialog<Screening> dialog = new Dialog<>();
        dialog.setTitle("Add Screening");
        dialog.setHeaderText("Enter screening details");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<MovieHall> hallComboBox = new ComboBox<>();
        List<MovieHall> movieHalls = selectedCinema.getMovieHalls();
//        System.out.println("CinemaMovieDetails: handleAddScreening: Movie halls: " + movieHalls);
        if (movieHalls != null && !movieHalls.isEmpty()) {
            hallComboBox.setItems(FXCollections.observableArrayList(movieHalls));

            hallComboBox.setConverter(new StringConverter<MovieHall>() {
                @Override
                public String toString(MovieHall hall) {
                    return hall != null ? "Hall " + hall.getHallNumber() : "";
                }

                @Override
                public MovieHall fromString(String string) {
                    return hallComboBox.getItems().stream()
                            .filter(hall -> ("Hall " + hall.getHallNumber()).equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });
        } else {
            showAlert("Error", "No movie halls available for the selected cinema.");
            return;
        }
        DatePicker datePicker = new DatePicker();
        setupDatePicker(datePicker);  // Set up the DatePicker with restrictions
        TextField timeField = new TextField();
        timeField.setPromptText("HH:mm");

        grid.add(new Label("Hall:"), 0, 0);
        grid.add(hallComboBox, 1, 0);
        grid.add(new Label("Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Time:"), 0, 2);
        grid.add(timeField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                try {
                    MovieHall selectedHall = hallComboBox.getValue();
                    LocalDate date = datePicker.getValue();
                    LocalTime time = LocalTime.parse(timeField.getText());
                    LocalDateTime startDateTime = LocalDateTime.of(date, time);
                    LocalDateTime endDateTime = startDateTime.plusMinutes(currentMovie.getDuration());

                    // Check if the hall is available
                    if (isHallAvailable(selectedHall, startDateTime, endDateTime)) {
                        Screening newScreening = new Screening(selectedCinema, selectedHall, currentMovie,
                                Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()),
                                new ArrayList<>(), false);
                        newScreening.setMovie(currentMovie);
                        return newScreening;
                    } else {
                        showAlert("Error", "The selected hall is not available at this time. Please choose another time or hall.");
                        return null;
                    }
                } catch (Exception e) {
                    showAlert("Error", "Invalid input. Please check your entries and try again.");
                    return null;
                }
            }
            return null;
        });

        Optional<Screening> result = dialog.showAndWait();
        result.ifPresent(screening -> {
            try {
                client.sendToServer(new Message(0, "addScreening", objectMapper.writeValueAsString(screening)));
                //handleCinemaSelection(); // Refresh the screenings list
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to add screening. Please try again.");
            }
        });
    }

    private boolean isHallAvailable(MovieHall hall, LocalDateTime start, LocalDateTime end) {
        Date startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
        System.out.println("Checking hall availability for " + hall.getHallNumber() + " on " + startDate + " - " + endDate);

        for (Screening screening : currentMovie.getScreenings()) {
            System.out.println("Checking screening: " + screening.getCinema().getCinemaName() + " - " + screening.getHall().getHallNumber() + " - " + screening.getTime());
            if (screening.getCinema().equals(cinemaComboBox.getValue()) && screening.getHall().getHallNumber() == (hall.getHallNumber())) {
                Date screeningEndTime = new Date(screening.getTime().getTime() + (currentMovie.getDuration() * 60 * 1000));
                System.out.println("Current screening: " + screening.getTime() + " - " + screeningEndTime);
                if ((startDate.before(screeningEndTime) && endDate.after(screening.getTime())) ||
                        (startDate.equals(screening.getTime()) || endDate.equals(screeningEndTime))) {
                    return false; // Conflict found
                }
            }
        }
        return true; // No conflicts found
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void onDeleteScreeningResponse(boolean success) {
        if (success) {
            showAlert("Success", "Screening deleted successfully.");
            // Refresh the list of screenings
            handleCinemaSelection();
        } else {
            showAlert("Error", "Failed to delete screening. Please try again.");
        }
    }

    @Subscribe
    public void onMovieDeleteEvent(MovieDeleteEvent event) {
        Movie deletedMovie = event.getMovie();
        String deletedType = event.getMovieType();
        if(deletedType.equals("cinema") && deletedMovie.getId()==currentMovie.getId())
        {
            if(client.isPersonNullOrCustomer()) {
                client.showAlert("Error", "This movie has been deleted, returning back to the movies list");
            }
            else {
                client.showSuccessAlert("The movie has been deleted successfully!");
            }
            cleanup();
            try {
                cleanup();
                App.setRoot("CinemaMovieList", null);
            } catch (IOException e) {
                System.out.println("CinemaMovieDetails: failed to get you back to CinemaMovieList");
                e.printStackTrace();

            }
        }
    }

    @Subscribe
    public void onMessageEvent(MessageEvent event) {
        Platform.runLater(this::handleCinemaSelection);
    }

}