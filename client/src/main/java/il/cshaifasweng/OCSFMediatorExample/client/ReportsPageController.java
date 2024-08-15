package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Cinema;
import il.cshaifasweng.OCSFMediatorExample.entities.CinemaManager;
import il.cshaifasweng.OCSFMediatorExample.entities.Person;
import il.cshaifasweng.OCSFMediatorExample.entities.Worker;
import il.cshaifasweng.OCSFMediatorExample.client.events.CinemaListEvent;
import il.cshaifasweng.OCSFMediatorExample.client.events.FailureEvent;
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


import java.util.Optional;
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
    @FXML
    private Label totalTicketTabsLabel;
    @FXML
    private Label totalLinksLabel;
    @FXML
    private Label totalComplaintsLabel;

    private SimpleClient client;
    private String currentReportData;

    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);
        setupComboBoxes();
        hideAllLabels();  // Call this here to ensure all are hidden initially
        exportButton.setDisable(true);
    }

    private void hideAllLabels() {
        // Ensure labels are not only invisible but also not managed by the layout
        totalTicketsLabel.setVisible(false);
        totalTicketsLabel.setManaged(false);
        totalTicketTabsLabel.setVisible(false);
        totalTicketTabsLabel.setManaged(false);
        totalLinksLabel.setVisible(false);
        totalLinksLabel.setManaged(false);
        totalComplaintsLabel.setVisible(false);
        totalComplaintsLabel.setManaged(false);
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
        System.out.println("inside init data before if data");
        if (data instanceof Worker) {
            Worker user = (Worker) data;
            System.out.println("inside init data before instance");
            boolean isCinemaManager = "CinemaManager".equalsIgnoreCase(user.getWorkerType());
            boolean isChainManager = "Chain manager".equalsIgnoreCase(user.getWorkerType());
    
            System.out.println("Worker Type: " + user.getWorkerType());
            System.out.println("Is Cinema Manager: " + isCinemaManager);
            System.out.println("Is Chain Manager: " + isChainManager);
    
            if (isCinemaManager && user instanceof CinemaManager) {
                CinemaManager cinemaManager = (CinemaManager) user;
                // Lock the cinema selection to the cinema managed by the CinemaManager
                cinemaComboBox.getItems().clear();
                cinemaComboBox.getItems().add(cinemaManager.getCinema().getCinemaName());
                cinemaComboBox.getSelectionModel().selectFirst();
                cinemaComboBox.setDisable(true);  // Disable cinema dropdown
        
                // Lock the report type to "Monthly Ticket Sales"
                reportTypeComboBox.getItems().clear();
                reportTypeComboBox.getItems().add("Monthly Ticket Sales");
                reportTypeComboBox.setValue("Monthly Ticket Sales");
                reportTypeComboBox.setDisable(true);  // Disable report type dropdown
        
                // Enable the export button since Cinema Managers should be able to export
                exportButton.setDisable(false);
            } else if (isChainManager) {
                // Chain manager can see all options
                cinemaComboBox.setVisible(true);
                reportTypeComboBox.setVisible(true);
                exportButton.setDisable(false);
            } else {
                // Other worker types: hide the report options
                cinemaComboBox.setVisible(false);
                reportTypeComboBox.setVisible(false);
                exportButton.setDisable(true);
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
        // This method now only updates the visibility of the cinema combo box based on the report type
        boolean showCinemaBox = "Monthly Ticket Sales".equals(reportTypeComboBox.getValue()) || 
                                "Customer Complaints Histogram".equals(reportTypeComboBox.getValue());
        cinemaComboBox.setVisible(showCinemaBox);
        cinemaComboBox.setManaged(showCinemaBox);
    }

    @FXML
    private void generateReport() {
        String reportType = reportTypeComboBox.getValue();
        YearMonth month = monthPicker.getValue();
        String cinema = cinemaComboBox.getValue();
    
        Person connectedPerson = client.getConnectedPerson();
        System.out.println("Connected person instance: " + connectedPerson.getClass().getName());
    
        if (connectedPerson instanceof Worker) {
            Worker worker = (Worker) connectedPerson;
            String workerType = worker.getWorkerType();
            System.out.println("Worker detected. Worker Type: " + workerType);
    
            if ("CinemaManager".equals(workerType)) {
                System.out.println("Connected person is a CinemaManager");
                reportType = "Monthly Ticket Sales Manager";
                // if (worker.getCinema() != null) {
                //     cinema = worker.getCinema().getCinemaName();
                // } else {
                //     cinema = "No Cinema Assigned";
                // }
                System.out.println("CinemaManager detected. Cinema: " + cinema);
            } else {
                System.out.println("Connected person is not a CinemaManager");
            }
        } else {
            System.out.println("Connected person is not a Worker");
        }
    
        if (reportType == null || month == null || cinema == null) {
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
        System.out.println("Received report data event: type=" + event.getReportType() + ", data=" + event.getReportData());
        Platform.runLater(() -> {
            currentReportData = event.getReportData();
            reportContainer.getChildren().clear();
            switch (event.getReportType()) {
                case "Monthly Ticket Sales":
                    System.out.println("Displaying Monthly Ticket Sales report");
                    displayTicketSalesReport(currentReportData);
                    break;
                case "Monthly Ticket Sales Manager":  // Add this case
                    System.out.println("Displaying Monthly Ticket Sales Manager report");
                    displayTicketSalesManagerReport(currentReportData);
                    break;
                case "Ticket Tab Sales":
                    System.out.println("Displaying Ticket Tab Sales report");
                    displayTabSalesReport(currentReportData);
                    break;
                case "Home Movie Link Sales":
                    System.out.println("Displaying Home Movie Link Sales report");
                    displayHomeMovieLinkSalesReport(currentReportData);
                    break;
                case "Customer Complaints Histogram":
                    System.out.println("Displaying Customer Complaints Histogram");
                    displayComplaintsHistogram(currentReportData);
                    break;
                default:
                    System.out.println("Unknown report type: " + event.getReportType());
                    break;
            }
        });
    }
    
    private void displayTicketSalesManagerReport(String reportData) {
        try {
            System.out.println("Report data received: " + reportData);
            String[] lines = reportData.split("\n");
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            
            barChart.setTitle("Monthly Ticket Sales for Cinema Manager");
            xAxis.setLabel("Day of Month");
            yAxis.setLabel("Number of Tickets Sold");
    
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ticket Sales");
            int totalTickets = 0;
    
            for (String line : lines) {
                System.out.println("Processing line: " + line);  // Add this for debugging
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

private void displayTabSalesReport(String reportData) {
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
    barChart.setTitle("Daily Ticket Tab Sales");
    xAxis.setLabel("Day of Month");
    yAxis.setLabel("Number of Tabs Sold");

    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Tabs Sold");
    int totalTabs = 0;  // This variable will be effectively final by the time it's used.

    String[] lines = reportData.split("\n");
    for (String line : lines) {
        String[] parts = line.split(": ");
        if (parts.length == 2) {
            int count = Integer.parseInt(parts[1]);
            series.getData().add(new XYChart.Data<>(parts[0], count));
            totalTabs += count;  // Total is accumulated here
        }
    }

    barChart.getData().add(series);
    reportContainer.getChildren().add(barChart);

    // Now we use totalTabs which is effectively final here
    if (totalTicketTabsLabel != null) {
        totalTicketTabsLabel.setText("Total Ticket Tabs: " + totalTabs);
    } else {
        System.err.println("Label totalTicketTabsLabel is not initialized.");
    }
}




private void displayHomeMovieLinkSalesReport(String reportData) {
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
    barChart.setTitle("Daily Home Movie Link Sales");
    xAxis.setLabel("Day of Month");
    yAxis.setLabel("Number of Links Sold");

    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Links Sold");
    int totalLinks = 0;

    String[] lines = reportData.split("\n");
    for (String line : lines) {
        String[] parts = line.split(": ");
        if (parts.length == 2) {
            series.getData().add(new XYChart.Data<>(parts[0], Integer.parseInt(parts[1])));
            totalLinks += Integer.parseInt(parts[1]);
        }
    }

    barChart.getData().add(series);
    reportContainer.getChildren().add(barChart);
    totalLinksLabel.setText("Total Home Movie Links: " + totalLinks); // Update total links label
}



private void displayComplaintsHistogram(String reportData) {
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
    barChart.setTitle("Customer Complaints Histogram");
    xAxis.setLabel("Day of Month");
    yAxis.setLabel("Number of Complaints");

    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Complaints");
    int totalComplaints = 0;
    String[] lines = reportData.split("\n");
    for (String line : lines) {
        String[] parts = line.split(": ");
        if (parts.length == 2) {
            series.getData().add(new XYChart.Data<>(parts[0], Integer.parseInt(parts[1])));
            totalComplaints += Integer.parseInt(parts[1]);
        }
    }

    barChart.getData().add(series);
    reportContainer.getChildren().add(barChart);
    totalComplaintsLabel.setText("Total Complaints: " + totalComplaints); // Update total complaints label
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

    @Subscribe
    public void onFailureEvent(FailureEvent event) {
    Platform.runLater(() -> {
        System.out.println("Report generation failed: " + event.getErrorMessage());
        showAlert("Report Generation Failed", event.getErrorMessage());
    });
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
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
