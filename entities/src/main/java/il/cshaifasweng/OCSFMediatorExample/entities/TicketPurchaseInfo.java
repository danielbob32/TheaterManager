package il.cshaifasweng.OCSFMediatorExample.entities;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TicketPurchaseInfo {
    private Screening screening;
    private List<Seat> selectedSeats;
    private int totalPrice;

    // Payment information
    private String name;
    private String id;
    private String email;
    private String paymentMethod;
    private String creditCardNumber;
    private String ticketTabNumber;
    private Date purchaseTime;
    private int screeningId;
    private List<Integer> selectedSeatIds;

    public TicketPurchaseInfo(Screening screening, List<Seat> selectedSeats, int totalPrice) {
        this.screening = screening;
        this.screeningId = screening.getScreening_id();
        this.selectedSeats = selectedSeats;
        this.selectedSeatIds = selectedSeats.stream().map(Seat::getSeat_id).collect(Collectors.toList());
        this.totalPrice = totalPrice;
        this.purchaseTime = new Date();
    }

    // Getters and setters for all fields

    public Screening getScreening() {
        return screening;
    }

    public void setScreening(Screening screening) {
        this.screening = screening;
    }

    public List<Seat> getSelectedSeats() {
        return selectedSeats;
    }

    public void setSelectedSeats(List<Seat> selectedSeats) {
        this.selectedSeats = selectedSeats;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getTicketTabNumber() {
        return ticketTabNumber;
    }

    public void setTicketTabNumber(String ticketTabNumber) {
        this.ticketTabNumber = ticketTabNumber;
    }

    public Date getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(Date purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public int getScreeningId() {
        return screeningId;
    }

    public List<Integer> getSelectedSeatIds() {
        return selectedSeatIds;
    }
}