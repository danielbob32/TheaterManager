package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Booking extends Product {
    private int bookingId;
    private Customer customer;
    private Date purchaseTime;
    private List<TicketTab> ticketTabs;
    private List<Ticket> tickets;

    @ManyToOne
    private Movie movie;

    @ManyToOne
    private Seat seat;

    @ManyToOne
    private MovieHall hall;

    // Constructors
    public Booking() {
    }

    public Booking(int bookingId, Customer customer, Date purchaseTime, List<TicketTab> ticketTabs, List<Ticket> tickets, Movie movie, Seat seat, MovieHall hall) {
        this.bookingId = bookingId;
        this.customer = customer;
        this.purchaseTime = purchaseTime;
        this.ticketTabs = ticketTabs;
        this.tickets = tickets;
        this.movie = movie;
        this.seat = seat;
        this.hall = hall;
    }

    // Getters and setters
    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public MovieHall getHall() {
        return hall;
    }

    public void setHall(MovieHall hall) {
        this.hall = hall;
    }

    public int getBookidId() {
        return bookingId;
    }

    public void setBookidId(int bookingId) {
        this.bookingId = bookingId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Date getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(Date purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public List<TicketTab> getTicketTabs() {
        return ticketTabs;
    }

    public void setTicketTabs(List<TicketTab> ticketTabs) {
        this.ticketTabs = ticketTabs;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }


    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
    }

    public void addTicketTab(TicketTab ticketTab) {
        this.ticketTabs.add(ticketTab);
    }

    public void removeTicket(Ticket ticket) {
        this.tickets.remove(ticket);
    }

    public void removeTicketTab(TicketTab ticketTab) {
        this.ticketTabs.remove(ticketTab);
    }

    public void removeAllTickets() {
        this.tickets.clear();
    }

    public void removeAllTicketTabs() {
        this.ticketTabs.clear();
    }

 
}
