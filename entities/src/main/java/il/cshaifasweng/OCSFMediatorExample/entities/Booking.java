package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Booking extends Product {
    private int bookingId;

    @ManyToOne(cascade = CascadeType.ALL)
    private Customer customer;

    private Date purchaseTime;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bookingId")
    private List<Product> products;


    @ManyToOne
    private Movie movie;

    @ManyToOne
    private Seat seat;

    @ManyToOne
    private MovieHall hall;

    // Constructors
    public Booking() {
    }

    public Booking(int bookingId, Customer customer, Date purchaseTime, List<Product> products, Movie movie, Seat seat, MovieHall hall) {
        this.bookingId = bookingId;
        this.customer = customer;
        this.purchaseTime = purchaseTime;
        this.products = products;
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

    public List<Product> getProducts() {
        return this.products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void addTicket(Ticket ticket) {
        this.products.add(ticket);
    }

    public void addTicketTab(TicketTab ticketTab) {
        this.products.add(ticketTab);
    }

    public void removeTicket(Ticket ticket) {
        this.products.remove(ticket);
    }

    public void removeTicketTab(TicketTab ticketTab) {
        this.products.remove(ticketTab);
    }

    public void removeAllProducts() {
        this.products.clear();
    }

}
