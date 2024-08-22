package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.PriceChangeRequest;

public class PriceChangeRequestEvent {
    private final String message;
    private final PriceChangeRequest request;
    private final boolean isSuccess;

    public PriceChangeRequestEvent(String message, PriceChangeRequest request, boolean isSuccess) {
        this.message = message;
        this.request = request;
        this.isSuccess = isSuccess;
    }

    public PriceChangeRequest getRequest() {
        return request;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }
}
