package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bookingId;

    @ManyToOne(cascade = CascadeType.ALL)
    private Customer customer;

    private Date purchaseTime;

    private String email;
    private String creditCard;
    private int ticketTabId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bookingId")
    private List<Product> products;


//    @ManyToOne
//    private Movie movie;
//
//    @ManyToOne
//    private Seat seat;
//
//    @ManyToOne
//    private MovieHall hall;

    // Constructors
    public Booking() {
        this.products = new ArrayList<>();
    }

    public Booking(Customer customer, Date purchaseTime, List<Product> products, Movie movie, Seat seat, MovieHall hall) {
        this.customer = customer;
        this.purchaseTime = purchaseTime;
        this.products = new ArrayList<>();
        this.creditCard = "";
        this.email = "";
        this.ticketTabId = 0;
//        this.movie = movie;
//        this.seat = seat;
//        this.hall = hall;
    }


    public Booking(Customer customer, Date purchaseTime, String email, String creditCard) {
        setCustomerAndBooking(customer);

        this.products = new ArrayList<>();
        this.purchaseTime = purchaseTime;
        this.email = email;
        this.creditCard = creditCard;
    }

    public Booking(Customer customer, Date purchaseTime, String email) {
        setCustomerAndBooking(customer);
        this.products = new ArrayList<>();
        this.purchaseTime = purchaseTime;
        this.email = email;
    }

//    public void setSeat(Seat seat) {
//        this.seat = seat;
//    }
//
//    public MovieHall getHall() {
//        return hall;
//    }
//
//    public void setHall(MovieHall hall) {
//        this.hall = hall;
//    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setCustomerAndBooking(Customer customer) {
        System.out.println("in Booking setCustomer");
        this.customer = customer;
        if (!customer.getBookings().contains(this)) {
            customer.getBookings().add(this);
        }
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

    public void addProduct(Product p)
    {
        this.products.add(p);
    }

    public void setTicketTabId(int ticketTabId) {
        this.ticketTabId = ticketTabId;
    }

    public int getTicketTabId() {
        return ticketTabId;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

}