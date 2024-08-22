package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.PriceChangeRequest;

import java.util.List;

public class PriceChangeRequestsListEvent {
    private final List<PriceChangeRequest> requests;
    private final boolean success;

    public PriceChangeRequestsListEvent(List<PriceChangeRequest> requests, boolean success) {
        this.requests = requests;
        this.success = success;
    }

    public List<PriceChangeRequest> getRequests() {
        return requests;
    }

    public boolean isSuccess() {
        return success;
    }

}
