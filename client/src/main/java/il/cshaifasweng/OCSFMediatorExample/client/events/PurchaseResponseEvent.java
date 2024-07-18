package il.cshaifasweng.OCSFMediatorExample.client.events;
import il.cshaifasweng.OCSFMediatorExample.entities.*;

public class PurchaseResponseEvent {
    private boolean success;
    private String message;
    private String data;

    public PurchaseResponseEvent(boolean success, String message, String data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getData() {
        return data;
    }

}