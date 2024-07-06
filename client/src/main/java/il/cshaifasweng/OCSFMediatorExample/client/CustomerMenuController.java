package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.fxml.FXML;

import java.io.IOException;

public class CustomerMenuController {

    @FXML
    private void viewMovieList() throws IOException {
        App.setRoot("MovieList");
    }

    @FXML
    private void buyTickets() throws IOException {
        App.setRoot("BuyTickets");
    }

    @FXML
    private void buyHomeTickets() throws IOException {
        App.setRoot("BuyHomeTickets");
    }

    @FXML
    private void buyTicketTab() throws IOException {
        App.setRoot("BuyTicketTab");
    }

    @FXML
    private void refundTickets() throws IOException {
        App.setRoot("RefundTickets");
    }

    @FXML
    private void fileComplaint() throws IOException {
        App.setRoot("FileComplaint");
    }
}