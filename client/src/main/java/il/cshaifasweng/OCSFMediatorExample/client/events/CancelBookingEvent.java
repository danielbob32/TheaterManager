package il.cshaifasweng.OCSFMediatorExample.client.events;

public class CancelBookingEvent {
    private int bookingId;
    private double refund;

    public CancelBookingEvent(int bookingId, double givenRefund) {
        this.bookingId = bookingId;
        this.refund = givenRefund;
    }
    public double getRefund() {
        return refund;
    }
    public int getBookingId() {
        return bookingId;
    }
}
