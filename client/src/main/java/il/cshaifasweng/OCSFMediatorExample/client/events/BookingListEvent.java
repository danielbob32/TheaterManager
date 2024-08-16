package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.Booking;
import java.util.List;

public class BookingListEvent {
    private int userId;
    private List<Booking> bookings;

    public BookingListEvent(int userId, List<Booking> bookings) {
        this.userId = userId;
        this.bookings = bookings;
    }

    public int getUserId() {
        return userId;
    }

    public List<Booking> getBookings() {
        return bookings;
    }
}
