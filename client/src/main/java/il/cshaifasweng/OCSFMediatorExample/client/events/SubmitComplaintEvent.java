package il.cshaifasweng.OCSFMediatorExample.client.events;

public class SubmitComplaintEvent {
    private final Object response;

    public SubmitComplaintEvent(Object response) {
        this.response = response;
    }

    public Object getResponse() {
        return response;
    }
}
