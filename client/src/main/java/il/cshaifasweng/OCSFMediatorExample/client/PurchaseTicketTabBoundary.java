package il.cshaifasweng.OCSFMediatorExample.client;
import il.cshaifasweng.OCSFMediatorExample.client.events.TicketTabListEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.Booking;
import il.cshaifasweng.OCSFMediatorExample.entities.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.TicketTab;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PurchaseTicketTabBoundary implements DataInitializable{

    private SimpleClient client;

    @FXML private AnchorPane buyTicketTabPane;
    @FXML private Stage stage;
    @FXML private Button buyTabButton;
    @FXML private Text totalText;
    @FXML private Button backButton;

    @FXML
    private TextField cardNumTextField, nameTextField, idTextField, emailTextField;

    private Customer customer;
    private String id;
    private String name;
    private String cardNum;
    private String email;
    private boolean isConnected = false;
    private List<TicketTab> allTicketTabs;

    @FXML private TableView<TicketTab> ticketTabTable;
    @FXML private TableColumn<TicketTab, Integer> ticketTabIdColumn;
    @FXML private TableColumn<TicketTab, String> purchaseTimeColumn;
    @FXML private TableColumn<TicketTab, Integer> ticketsLeftColumn;
    @FXML private TableColumn<TicketTab, String> statusColumn;

    @Override
    public void setClient(SimpleClient client) { this.client = client; }

    @Override
    public void initData(Object data) {
        initialize();
        ticketTabTable.setVisible(false);

        // if customer is already in the system
        if (data instanceof Customer) {
            isConnected = true;
            this.customer = (Customer) data;
            nameTextField.setText(customer.getName());
            idTextField.setText(String.valueOf(customer.getPersonId()));
            emailTextField.setText(customer.getEmail());
            try {
                initializeTable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void initialize() {
        // Check if already registered before registering
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        totalText.setText("₪200");
        addTextListener(cardNumTextField);
        addTextListener(nameTextField);
        addTextListener(idTextField);
        addTextListener(emailTextField);
    }

    @FXML
    public void initializeTable() throws IOException {
        ticketTabTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        //ticketTabTable.setVisible(true);

        ticketTabIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getProduct_id()).asObject());
        ticketsLeftColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAmount()).asObject());
        purchaseTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPurchaseTime().toString()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));

        if (client.getConnectedPerson() != null) {
            client.fetchUserTicketTabs();
        }
    }

    @Subscribe
    public void onTicketTabsListEvent(TicketTabListEvent event) {
        allTicketTabs = event.getTicketTabs();
        if (allTicketTabs.size() > 0) {
            ticketTabTable.setVisible(true);
        }
        Platform.runLater(() -> {
            ticketTabTable.getItems().clear();
            ticketTabTable.getItems().addAll(allTicketTabs);
        });
    }

    @FXML
    void handlePurchaseTicketTab(ActionEvent event) {
        try {
            validateInput();
            name = nameTextField.getText();
            cardNum = cardNumTextField.getText();
            email = emailTextField.getText();
            id = idTextField.getText();
            System.out.println(name + " clicked buy ticket tab");
            client.purchaseTicketTab(id, name, email, cardNum);

        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());
        }
    }


    @FXML
    void handleBackButton(ActionEvent event) throws IOException {
        if (isConnected) App.setRoot("CustomerMenu", customer);
        else App.setRoot("Loginpage", null);
    }

    private void clearInputFields() {
        nameTextField.clear();
        idTextField.clear();
        emailTextField.clear();
        cardNumTextField.clear();
    }

    private void validateInput() throws ValidationException {
        validateId();
        validateEmail();
        validateName();
        validateCardNum();
    }

    private void validateCardNum() throws ValidationException {
        String cardNum = cardNumTextField.getText();
        if (cardNum.isBlank()) {
            throw new ValidationException("Card number cannot be empty.");
        }
        if (!isCardNumValid(cardNum)) {
            throw new ValidationException("Invalid card number.");
        }
    }

    private void validateName() throws ValidationException {
        String name = nameTextField.getText().trim();
        if (!Pattern.matches("[a-zA-Z\\s]+", name)) {
            throw new ValidationException("Invalid name. Please use only letters and spaces.");
        }
    }

    private void validateId() throws ValidationException {
        String id = idTextField.getText();
        if (id.isBlank()) {
            throw new ValidationException("ID cannot be empty.");
        }
        if (!isAllNums(id) || (id.length() != 9 && id.length() != 4)) {
            throw new ValidationException("Invalid ID.");
        }
    }

    private void validateEmail() throws ValidationException {
        String email = emailTextField.getText();
        if (email.isBlank()) {
            throw new ValidationException("Email cannot be empty.");
        }
        if (!isEmailValid(email)) {
            throw new ValidationException("Invalid email format.");
        }
    }

    private boolean isCardNumValid(String cardNum) {
        return cardNum.length() == 10 && isAllNums(cardNum);
    }

    private static boolean isAllNums(String s) {
        return s != null && s.matches("[0-9]*");
    }

    private static boolean isEmailValid(String email) {
        // Regular expression for a valid email address
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void addTextListener(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

}