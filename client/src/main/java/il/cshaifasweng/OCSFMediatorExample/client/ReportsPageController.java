package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Cinema;
import il.cshaifasweng.OCSFMediatorExample.entities.CinemaManager;
import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import il.cshaifasweng.OCSFMediatorExample.client.events.CinemaListEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.ReportDataEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class ReportsPageController implements DataInitializable {

    @FXML
    private ComboBox<String> reportTypeComboBox;
    @FXML
    private ComboBox<YearMonth> monthPicker;
    @FXML
    private ComboBox<String> cinemaComboBox;
    @FXML
    private VBox reportContainer;
    @FXML
    private Button exportButton;
    @FXML
    private Label totalTicketsLabel;

    private SimpleClient client;
    private String currentReportData;

    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);
        setupComboBoxes();
        updateUIBasedOnReportType();
        exportButton.setDisable(true);
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
                boolean isBranchManager = "Chain manager".equals(worker.getWorkerType());
                cinemaComboBox.setVisible(isBranchManager);
                cinemaComboBox.setManaged(isBranchManager);
                if (isBranchManager) {
                    try {
                        client.requestCinemaList();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert("Error requesting cinema list: " + e.getMessage());
                    }
                } else if (worker instanceof CinemaManager) {
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

    private void setupComboBoxes() {
        reportTypeComboBox.getItems().addAll(
            "Monthly Ticket Sales",
            "Ticket Tab Sales",
            "Home Movie Link Sales",
            "Customer Complaints Histogram"
        );
        reportTypeComboBox.setOnAction(event -> updateUIBasedOnReportType());

        reportTypeComboBox.setValue("Monthly Ticket Sales");

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

        reportTypeComboBox.setEditable(false);
        monthPicker.setEditable(false);
        cinemaComboBox.setEditable(false);

        reportTypeComboBox.setStyle("-fx-alignment: CENTER;");
        monthPicker.setStyle("-fx-alignment: CENTER;");
        cinemaComboBox.setStyle("-fx-alignment: CENTER;");
    }

    private void updateUIBasedOnReportType() {
        String reportType = reportTypeComboBox.getValue();
        boolean showCinemaBox = reportType != null &&
            (reportType.equals("Monthly Ticket Sales") || reportType.equals("Customer Complaints Histogram"));
        cinemaComboBox.setVisible(showCinemaBox);
        cinemaComboBox.setManaged(showCinemaBox);
    }

    @FXML
    private void generateReport() {
        String reportType = reportTypeComboBox.getValue();
        YearMonth month = monthPicker.getValue();
        String cinema = cinemaComboBox.getValue();

        if (reportType == null || month == null || (cinemaComboBox.isVisible() && cinema == null)) {
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
            currentReportData = event.getReportData();
            reportContainer.getChildren().clear();
            switch (event.getReportType()) {
                case "Monthly Ticket Sales":
                    displayTicketSalesReport(currentReportData);
                    break;
                case "Ticket Tab Sales":
                case "Home Movie Link Sales":
                    displaySalesReport(event.getReportType(), currentReportData);
                    break;
                case "Customer Complaints Histogram":
                    displayComplaintsHistogram(currentReportData);
                    break;
            }
            exportButton.setDisable(false);
        });
    }

private void displayTicketSalesReport(String reportData) {
    try {
        String[] lines = reportData.split("\n");
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        
        barChart.setTitle("Daily Ticket Sales");
        xAxis.setLabel("Day of Month");
        yAxis.setLabel("Number of Tickets Sold");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ticket Sales");
        int totalTickets = 0;

        for (String line : lines) {
            String[] parts = line.split(": ");
            if (parts.length == 2) {
                int tickets = Integer.parseInt(parts[1]);
                series.getData().add(new XYChart.Data<>(parts[0], tickets));
                totalTickets += tickets;
            }
        }

        barChart.getData().add(series);
        reportContainer.getChildren().add(barChart);
        totalTicketsLabel.setText("Total Tickets: " + totalTickets); // Update total tickets label
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Error parsing ticket sales report data: " + e.getMessage());
    }
}

    private void displaySalesReport(String reportType, String reportData) {
        VBox reportBox = new VBox(10);

        Label titleLabel = new Label(reportType);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        reportBox.getChildren().add(titleLabel);

        Label dataLabel = new Label(reportData);
        reportBox.getChildren().add(dataLabel);

        reportContainer.getChildren().add(reportBox);
    }

    private void displayComplaintsHistogram(String reportData) {
        try {
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

            barChart.setTitle("Customer Complaints Histogram");
            xAxis.setLabel("Day of Month");
            yAxis.setLabel("Number of Complaints");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Complaints");

            String[] lines = reportData.split("\n");
            for (String line : lines) {
                String[] parts = line.split(": ");
                if (parts.length == 2) {
                    series.getData().add(new XYChart.Data<>(parts[0], Integer.parseInt(parts[1])));
                }
            }

            barChart.getData().add(series);
            reportContainer.getChildren().add(barChart);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error parsing complaints histogram data: " + e.getMessage());
        }
    }

    @FXML
    private void exportToExcel() {
        if (currentReportData == null || currentReportData.isEmpty()) {
            showAlert("No report data available. Please generate a report first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(reportTypeComboBox.getValue());
                String[] lines = currentReportData.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    Row row = sheet.createRow(i);
                    String[] parts = lines[i].split(": ");
                    row.createCell(0).setCellValue(parts[0]);
                    if (parts.length > 1) {
                        try {
                            row.createCell(1).setCellValue(Integer.parseInt(parts[1].trim()));
                        } catch (NumberFormatException e) {
                            row.createCell(1).setCellValue(parts[1].trim());
                        }
                    }
                }

                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    workbook.write(outputStream);
                }

                showAlert("Export Successful", "Report exported to Excel successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Export Failed", "Failed to export report: " + e.getMessage());
            }
        }
    }

    private void showAlert(String message) {
        showAlert("Alert", message);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
            currentReportData = null;
            exportButton.setDisable(true);
        });
    }

    public void cleanup() {
        EventBus.getDefault().unregister(this);
    }

    @FXML
    private void handleBackButton() throws IOException {
        Person connectedPerson = client.getConnectedPerson();
        App.setRoot("WorkerMenu", connectedPerson);
    }

}
