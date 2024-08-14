package il.cshaifasweng.OCSFMediatorExample.client.events;

public class MovieUpdateEvent {
    private boolean isSuccess;

    public MovieUpdateEvent(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
