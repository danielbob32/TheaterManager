package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.Booking;
import il.cshaifasweng.OCSFMediatorExample.entities.TicketTab;

import java.util.List;

public class TicketTabListEvent {

    private int userId;
    private List<TicketTab> ticketTabs;

    public TicketTabListEvent(int userId, List<TicketTab> ticketTabs) {
        this.userId = userId;
        this.ticketTabs = ticketTabs;
    }

    public int getUserId() {
        return userId;
    }

    public List<TicketTab> getTicketTabs() {
        return ticketTabs;
    }
}