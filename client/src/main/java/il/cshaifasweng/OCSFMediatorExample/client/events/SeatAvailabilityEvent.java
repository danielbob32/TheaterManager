package il.cshaifasweng.OCSFMediatorExample.client.events;
import il.cshaifasweng.OCSFMediatorExample.entities.*;

import java.util.List;

public class SeatAvailabilityEvent {
    private int screeningId;
    private List<Seat> seats;

    public SeatAvailabilityEvent(int screeningId, List<Seat> seats) {
        this.screeningId = screeningId;
        this.seats = seats;
    }

    public int getScreeningId() {
        return screeningId;
    }

    public List<Seat> getSeats() {
        return seats;
    }
}