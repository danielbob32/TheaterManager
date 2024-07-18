package il.cshaifasweng.OCSFMediatorExample.client.events;

public class TicketTabResponseEvent {
    private final boolean isValid;
    private final String ticketTabNumber;

    public TicketTabResponseEvent(boolean isValid, String ticketTabNumber) {
        this.isValid = isValid;
        this.ticketTabNumber = ticketTabNumber != null ? ticketTabNumber : "";
    }

    public boolean isValid() {
        return isValid;
    }

    public String getTicketTabNumber() {
        return ticketTabNumber;
    }
}