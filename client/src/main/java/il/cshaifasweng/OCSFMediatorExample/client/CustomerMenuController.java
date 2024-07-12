package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.fxml.FXML;

import java.io.IOException;

public class CustomerMenuController implements DataInitializable{

    private SimpleClient client;

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
    }

    @Override
    public void initData(Object data) {
        System.out.println("CustomerMenuController initialized");
    }

    @FXML
    private void viewMovieList() throws IOException {
        App.setRoot("CinemaMovieList", null);
    }

    @FXML
    private void buyTickets() throws IOException {
        App.setRoot("BuyTickets", null);
    }

    @FXML
    private void buyHomeTickets() throws IOException {
        App.setRoot("BuyHomeTickets", null);
    }

    @FXML
    private void buyTicketTab() throws IOException {
        App.setRoot("BuyTicketTab", null);
    }

    @FXML
    private void refundTickets() throws IOException {
        App.setRoot("RefundTickets", null);
    }

    @FXML
    private void fileComplaint() throws IOException {
        App.setRoot("FileComplaint", null);
    }

    @FXML
    private void handleLogout() throws IOException {
        // TODO: Send logout request to server if necessary
        App.setRoot("LoginPage", null);
    }
}