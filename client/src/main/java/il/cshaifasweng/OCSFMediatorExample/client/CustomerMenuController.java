package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class CustomerMenuController implements DataInitializable{

    private SimpleClient client;

    @FXML
    private Label welcomeLabel;

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        System.out.println("CustomerMenuController initialized");
        updateWelcomeMessage();
    }

    private void updateWelcomeMessage() {
        Person connectedPerson = client.getConnectedPerson();
        if (connectedPerson != null) {
            welcomeLabel.setText("Welcome, " + connectedPerson.getName());
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

        App.setRoot("HomeMovieList", connectedPerson);
    }

    @FXML
    private void viewMovieList() throws IOException {
        Person connectedPerson = client.getConnectedPerson();

        App.setRoot("CinemaMovieList", connectedPerson);
    }

//    @FXML
//    private void buyTickets() throws IOException {
//        Person connectedPerson = client.getConnectedPerson();
//
//        App.setRoot("BuyTickets", connectedPerson);
//    }

//    @FXML
//    private void buyHomeTickets() throws IOException {
//        Person connectedPerson = client.getConnectedPerson();
//
//        App.setRoot("BuyHomeTickets", connectedPerson);
//    }

    @FXML
    private void buyTicketTab() throws IOException {
        Person connectedPerson = client.getConnectedPerson();

        App.setRoot("PurchaseTicketTab", connectedPerson);
    }

    @FXML
    private void refundTickets() throws IOException {
        Person connectedPerson = client.getConnectedPerson();

        App.setRoot("RefundTickets", connectedPerson);
    }

    @FXML
    private void fileComplaint() throws IOException {
        Person connectedPerson = client.getConnectedPerson();

        App.setRoot("FileComplaint", connectedPerson);
    }

    @FXML
    private void handleLogout() throws IOException {
        client.logout();
        App.setRoot("LoginPage", null);
    }
}