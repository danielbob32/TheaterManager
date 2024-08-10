package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.application.Platform;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import java.time.LocalDate;
import java.time.YearMonth;
import java.io.IOException;

import il.cshaifasweng.OCSFMediatorExample.client.events.CinemaListEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.ReportDataEvent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javafx.util.StringConverter;

public class ReportsPageController implements DataInitializable {

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private ComboBox<YearMonth> monthPicker;
    @FXML private ComboBox<String> cinemaComboBox;
    @FXML private VBox reportContainer;
    @FXML private Button backButton;

    private SimpleClient client;
    private BooleanProperty isBranchManager = new SimpleBooleanProperty(false);
    private ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);
        reportTypeComboBox.getItems().addAll(
            "Monthly Ticket Sales",
            "Ticket Tabs and Home Movie Links Sales",
            "Customer Complaints Histogram"
        );
        cinemaComboBox.visibleProperty().bind(isBranchManager);
        
        // Initialize monthPicker
        YearMonth currentMonth = YearMonth.now();
        for (int i = 0; i < 12; i++) {
            monthPicker.getItems().add(currentMonth.minusMonths(i));
        }
        monthPicker.setValue(currentMonth);
        monthPicker.setConverter(new StringConverter<YearMonth>() {
            @Override
            public String toString(YearMonth yearMonth) {
                return yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            }
    
            @Override
            public YearMonth fromString(String string) {
                return YearMonth.parse(string, DateTimeFormatter.ofPattern("MMMM yyyy"));
            }
        });
    }

    @Override
    public void setClient(SimpleClient client) {
        this.client = client;
        if (client != null) {
            try {
                client.requestCinemaList();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error requesting cinema list: " + e.getMessage());
            }
        }
    }

    @Override
    public void initData(Object data) {
        if (data instanceof Person) {
            Person user = (Person) data;
            if (user instanceof Worker) {
                Worker worker = (Worker) user;
                isBranchManager.set("Chain manager".equals(worker.getWorkerType()));
                if (isBranchManager.get()) {
                    try {
                        client.requestCinemaList();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert("Error requesting cinema list: " + e.getMessage());
                    }
                } else {
                    // Check if the worker is a CinemaManager
                    if (worker instanceof CinemaManager) {
                        CinemaManager cinemaManager = (CinemaManager) worker;
                        Cinema managerCinema = cinemaManager.getCinema();
                        if (managerCinema != null) {
                            cinemaComboBox.getItems().add(managerCinema.getCinemaName());
                            cinemaComboBox.getSelectionModel().selectFirst();
                        } else {
                            System.err.println("CinemaManager has no associated cinema");
                        }
                    } else {
                        System.err.println("Worker is not a CinemaManager");
                    }
                }
            }
        }
    }

    @FXML
    private void generateReport() {
        String reportType = reportTypeComboBox.getValue();
        YearMonth month = monthPicker.getValue();
        String cinema = cinemaComboBox.getValue();

        if (reportType == null || month == null || (isBranchManager.get() && cinema == null)) {
            showAlert("Please select all required fields.");
            return;
        }

        try {
            client.requestReport(reportType, month.atDay(1), cinema);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error requesting report: " + e.getMessage());
            clearReportContainer();
        }
    }

    @Subscribe
    public void onReportDataReceived(ReportDataEvent event) {
        Platform.runLater(() -> {
            reportContainer.getChildren().clear();
            switch (event.getReportType()) {
                case "Monthly Ticket Sales":
                    displayTicketSalesReport(event.getReportData());
                    break;
                case "Ticket Tabs and Home Movie Links Sales":
                    displayTabsAndLinksReport(event.getReportData());
                    break;
                case "Customer Complaints Histogram":
                    displayComplaintsHistogram(event.getReportData());
                    break;
            }
        });
    }

    private void displayTicketSalesReport(String reportData) {
        try {
            if (reportData.startsWith("Error")) {
                throw new Exception(reportData);
            }
            JsonNode rootNode = objectMapper.readTree(reportData);
            
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            
            barChart.setTitle("Monthly Ticket Sales");
            xAxis.setLabel("Cinema");
            yAxis.setLabel("Number of Tickets Sold");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ticket Sales");

            rootNode.fields().forEachRemaining(entry -> {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().asInt()));
            });

            barChart.getData().add(series);
            reportContainer.getChildren().add(barChart);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error parsing ticket sales report data: " + e.getMessage());
        }
    }

    private void displayTabsAndLinksReport(String reportData) {
        try {
            JsonNode rootNode = objectMapper.readTree(reportData);
            
            VBox reportBox = new VBox(10);
            
            Label titleLabel = new Label("Ticket Tabs and Home Movie Links Sales");
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            reportBox.getChildren().add(titleLabel);

            rootNode.fields().forEachRemaining(entry -> {
                Label itemLabel = new Label(entry.getKey() + ": " + entry.getValue().asInt());
                reportBox.getChildren().add(itemLabel);
            });

            reportContainer.getChildren().add(reportBox);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error parsing tabs and links report data: " + e.getMessage());
        }
    }

    private void displayComplaintsHistogram(String reportData) {
        try {
            JsonNode rootNode = objectMapper.readTree(reportData);
            
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            
            barChart.setTitle("Customer Complaints Histogram");
            xAxis.setLabel("Day of Month");
            yAxis.setLabel("Number of Complaints");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Complaints");

            rootNode.fields().forEachRemaining(entry -> {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().asInt()));
            });

            barChart.getData().add(series);
            reportContainer.getChildren().add(barChart);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error parsing complaints histogram data: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

    @FXML
    private void handleBackButton() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        App.setRoot("WorkerMenu", connectedPerson);
    }

    @Subscribe
    public void onCinemaListReceived(CinemaListEvent event) {
        Platform.runLater(() -> {
            cinemaComboBox.getItems().clear();
            cinemaComboBox.getItems().addAll(event.getCinemas());
            cinemaComboBox.getItems().add(0, "All");
            cinemaComboBox.getSelectionModel().selectFirst();
        });
    }

    private void clearReportContainer() {
        Platform.runLater(() -> {
            reportContainer.getChildren().clear();
        });
    }
}