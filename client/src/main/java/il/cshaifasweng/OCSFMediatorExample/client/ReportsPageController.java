package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.client.events.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportsPageController implements DataInitializable {

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private ComboBox<YearMonth> monthPicker;
    @FXML private ComboBox<String> cinemaComboBox;
    @FXML private VBox reportContainer;
    @FXML private Button exportButton;
    @FXML private Label totalTicketsLabel;
    @FXML private Label totalTicketTabsLabel;
    @FXML private Label totalLinksLabel;
    @FXML private Label totalComplaintsLabel;

    private SimpleClient client;
    private String currentReportData;
    private String cinemaName;
    private static final String MONTHLY_SALES_COLOR = " #4CAF50";
    private static final String TICKET_TAB_COLOR = " #e156c3";
    private static final String HOME_MOVIE_COLOR = " #FFC107";
    private static final String COMPLAINTS_COLOR = " #FF5722";

    @FXML
    public void initialize() {
        EventBus.getDefault().register(this);
        setupComboBoxes();
        hideAllLabels();
        exportButton.setDisable(true);
    }

    private void hideAllLabels() {
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
            client.requestCinemaList();
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
                cinemaComboBox.setVisible(false);
                cinemaComboBox.setManaged(false);
                
                // Lock the report type to "Monthly Ticket Sales"
                reportTypeComboBox.setVisible(false);
                reportTypeComboBox.setManaged(false);
                
                // Set the cinema name for the report
                this.cinemaName = cinemaManager.getCinema().getCinemaName();
                
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
            @Override
            public String toString(YearMonth yearMonth) {
                return yearMonth.format(formatter);
            }
            @Override
            public YearMonth fromString(String string) {
                return YearMonth.parse(string, formatter);
            }
        });

        reportTypeComboBox.setEditable(false);
        monthPicker.setEditable(false);
        cinemaComboBox.setEditable(false);
    }

    private void updateUIBasedOnReportType() {
        boolean showCinemaBox = "Monthly Ticket Sales".equals(reportTypeComboBox.getValue()) || 
                                "Customer Complaints Histogram".equals(reportTypeComboBox.getValue());
        cinemaComboBox.setVisible(showCinemaBox);
        cinemaComboBox.setManaged(showCinemaBox);
    }

    @FXML
    private void generateReport() {
        YearMonth month = monthPicker.getValue();
        Person connectedPerson = client.getConnectedPerson();
        String reportType;
        String cinema;
    
        if (connectedPerson instanceof Worker) {
            Worker worker = (Worker) connectedPerson;
            if ("CinemaManager".equals(worker.getWorkerType())) {
                reportType = "Monthly Ticket Sales Manager";
                cinema = this.cinemaName;
            } else {
                reportType = reportTypeComboBox.getValue();
                cinema = cinemaComboBox.getValue();
            }
        } else {
            reportType = reportTypeComboBox.getValue();
            cinema = cinemaComboBox.getValue();
        }
    
        System.out.println("Generating report - Type: " + reportType + ", Month: " + month + ", Cinema: " + cinema);
    
        if (reportType == null || month == null || cinema == null) {
            showAlert("Please select all required fields.");
            return;
        }
    
        hideAllLabels();
    
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
                case "Monthly Ticket Sales Manager":
                    displayTicketSalesReport(currentReportData, event.getReportType());
                    break;
                case "Ticket Tab Sales":
                    displayTabSalesReport(currentReportData);
                    break;
                case "Home Movie Link Sales":
                    displayHomeMovieLinkSalesReport(currentReportData);
                    break;
                case "Customer Complaints Histogram":
                    displayComplaintsHistogram(currentReportData);
                    break;
                default:
                    System.out.println("Unknown report type: " + event.getReportType());
                    break;
            }
        });
    }

    @Subscribe
    public void onPurchaseResponseEvent(PurchaseResponseEvent event)
    {
        if(event.isSuccess())
            generateReport();
    }

    @Subscribe
    public void onSubmitComplaintEvent(SubmitComplaintEvent event)
    {
        generateReport();
    }

    @Subscribe
    public void onTicketTabPurchaseEvent(TicketTabPurchaseEvent event) {
        if(event.isSuccess())
            generateReport();
    }

    @Subscribe
    public void onHomeLinkPurchaseResponseEvent(HomeLinkPurchaseResponseEvent event)
    {
        System.out.println("ReportsPageController: onHomeLinkPurchaseResponseEvent: success:"+event.isSuccess());
        if(event.isSuccess())
        {
            generateReport();
        }
    }


    
    private void displayTicketSalesReport(String reportData, String reportType) {
        try {
            String[] lines = reportData.split("\n");
            if (lines.length < 2) {
                showAlert("No valid data available in the report.");
                return;
            }
    
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            
            boolean isCinemaManager = reportType.equals("Monthly Ticket Sales Manager");
            boolean isAllCinemas = cinemaComboBox.getValue().equals("All");
            
            if (isCinemaManager) {
                String cinemaName = lines[0].split("for ")[1].split(" -")[0];
                barChart.setTitle("Monthly Ticket Sales for " + cinemaName);
                xAxis.setLabel("Day of Month");
            } else if (isAllCinemas) {
                barChart.setTitle("Monthly Ticket Sales - All Cinemas");
                xAxis.setLabel("Cinema");
            } else {
                String cinemaName = cinemaComboBox.getValue();
                barChart.setTitle("Monthly Ticket Sales for " + cinemaName);
                xAxis.setLabel("Day of Month");
            }
            
            yAxis.setLabel("Number of Tickets Sold");
        
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ticket Sales");
            int totalTickets = 0;
    
            for (int i = 2; i < lines.length; i++) {
                if (lines[i].trim().isEmpty()) continue;
                
                String[] parts = lines[i].split(": ");
                if (parts.length == 2) {
                    try {
                        String key = parts[0].trim();
                        int tickets = Integer.parseInt(parts[1].trim());
                        if (isCinemaManager || !isAllCinemas) {
                            key = "Day " + key; // Prefix with "Day" for single cinema reports
                        }
                        series.getData().add(new XYChart.Data<>(key, tickets));
                        totalTickets += tickets;
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping line due to number format issue: " + lines[i]);
                    }
                }
            }
    
            barChart.getData().add(series);
            barChart.setLegendVisible(false);
            setChartColor(barChart, MONTHLY_SALES_COLOR);
            reportContainer.getChildren().add(barChart);
            totalTicketsLabel.setText("Total Tickets: " + totalTickets);
            totalTicketsLabel.setVisible(true);
            totalTicketsLabel.setManaged(true);
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
        int totalTabs = 0;

        String[] lines = reportData.split("\n");
        for (String line : lines) {
            String[] parts = line.split(": ");
            if (parts.length == 2) {
                int count = Integer.parseInt(parts[1]);
                series.getData().add(new XYChart.Data<>(parts[0], count));
                totalTabs += count;
            }
        }

        barChart.getData().add(series);
        barChart.setLegendVisible(false);
        setChartColor(barChart, TICKET_TAB_COLOR);
        reportContainer.getChildren().add(barChart);

        totalTicketTabsLabel.setText("Total Ticket Tabs: " + totalTabs);
        totalTicketTabsLabel.setVisible(true);
        totalTicketTabsLabel.setManaged(true);
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
        barChart.setLegendVisible(false);
        setChartColor(barChart, HOME_MOVIE_COLOR);
        reportContainer.getChildren().add(barChart);
        totalLinksLabel.setText("Total Home Movie Links: " + totalLinks); 
        totalLinksLabel.setVisible(true);
        totalLinksLabel.setManaged(true);
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
        barChart.setLegendVisible(false);
        setChartColor(barChart, COMPLAINTS_COLOR);
        reportContainer.getChildren().add(barChart);
        totalComplaintsLabel.setText("Total Complaints: " + totalComplaints); 
        totalComplaintsLabel.setVisible(true);
        totalComplaintsLabel.setManaged(true);
    }

    private void setChartColor(BarChart<String, Number> barChart, String color) {
        for (XYChart.Series<String, Number> series : barChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                data.getNode().setStyle("-fx-bar-fill: " + color + ";");
            }
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
    public void onCinemaListEvent(CinemaListEvent event) {
        Platform.runLater(() -> {
            cinemaComboBox.getItems().clear();
            List<String> cinemaNames = new ArrayList<>();
            for(Cinema c : event.getCinemas())
            {
                cinemaNames.add(c.getCinemaName());
            }
            cinemaComboBox.getItems().addAll(cinemaNames);
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
        cleanup();
        App.setRoot("WorkerMenu", connectedPerson);
    }

}
