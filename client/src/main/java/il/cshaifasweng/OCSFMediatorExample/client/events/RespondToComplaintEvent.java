package il.cshaifasweng.OCSFMediatorExample.client.events;

public class RespondToComplaintEvent {
    private final String response;

    public RespondToComplaintEvent(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
