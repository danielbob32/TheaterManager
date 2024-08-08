package il.cshaifasweng.OCSFMediatorExample.client.events;
import il.cshaifasweng.OCSFMediatorExample.entities.*;

public class PurchaseResponseEvent {
    private boolean success;
    private String message;
    private String data;
    private Object linkdata;

    public PurchaseResponseEvent(boolean success, String message, String data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public PurchaseResponseEvent(boolean success, String message) {
        this.success = success;
        this.message = message;
    }


    public PurchaseResponseEvent(boolean success, String message, Object linkdata) {
        this.success = success;
        this.message = message;
        this.linkdata = linkdata;
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

    public Object getLinkData() {
        return linkdata;
    }
    
    public void setData(Object linkdata) {
        this.linkdata = linkdata;
    }

}