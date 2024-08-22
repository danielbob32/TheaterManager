package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.Complaint;

public class SubmitComplaintEvent {
    private final Complaint complaint;

    public SubmitComplaintEvent(Complaint complaint) {
        this.complaint = complaint;
    }

    public Complaint getComplaint() {
        return this.complaint;
    }
}
