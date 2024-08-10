package il.cshaifasweng.OCSFMediatorExample.client.events;

public class ReportDataEvent {
    private String reportType;
    private String reportData;

    public ReportDataEvent(String reportType, String reportData) {
        this.reportType = reportType;
        this.reportData = reportData;
    }

    public String getReportType() {
        return reportType;
    }

    public String getReportData() {
        return reportData;
    }
}