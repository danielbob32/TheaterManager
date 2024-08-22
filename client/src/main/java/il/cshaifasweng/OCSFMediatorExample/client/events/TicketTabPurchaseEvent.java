package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.TicketTab;

public class TicketTabPurchaseEvent {
    private boolean success;
    private final String data;

    public TicketTabPurchaseEvent(boolean success, String data) {
        this.success = success;
        this.data = data;
    }
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getData() {
        return this.data;
    }
}
