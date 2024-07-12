package il.cshaifasweng.OCSFMediatorExample.client.events;

public class FailureEvent {
    private final String errorMessage;

    public FailureEvent(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
