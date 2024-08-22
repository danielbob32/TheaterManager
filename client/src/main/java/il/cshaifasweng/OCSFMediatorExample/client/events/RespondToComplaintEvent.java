package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.Complaint;

public class RespondToComplaintEvent {
    private final Complaint complaint;

    public RespondToComplaintEvent(Complaint complaint) {
        this.complaint = complaint;
    }

    public Complaint getComplaint() {
        return complaint;
    }
}
