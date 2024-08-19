package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.Complaint;
import java.util.List;

public class CustomerComplaintListEvent {
    private final List<Complaint> complaints;

    public CustomerComplaintListEvent(List<Complaint> complaints) {
        this.complaints = complaints;
    }

    public List<Complaint> getComplaints() {
        return complaints;
    }
}
