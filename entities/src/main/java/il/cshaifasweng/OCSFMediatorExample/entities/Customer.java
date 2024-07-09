package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Entity

public class Customer extends Person{


    private String email;

    @OneToMany(mappedBy = "customer")
    private List<Booking> bookings;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "customer_id")
    private List<Product> products;

    @OneToMany(mappedBy = "customer")
    private List<Complaint> complaints;

    // Constructors
    public Customer() {
    }

    public Customer(String name, String email, int id) {
        super(name, id);
        this.email = email;
    }

    // Getters and setters



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

   public List<Booking> getBookings() {
       return bookings;
   }

   public void setBookings(List<Booking> bookings) {
       this.bookings = bookings;
   }

    public List<HomeMovieLink> getHomeMovies() {
        List<HomeMovieLink> homeLinks = new ArrayList<HomeMovieLink>();
        for(Product product : products) {
            if(product instanceof HomeMovieLink) {
                homeLinks.add((HomeMovieLink) product);
            }
        }
        return homeLinks;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<TicketTab> getTicketTabs() {
        List<TicketTab> tabs = new ArrayList<TicketTab>();
        for(Product product : products) {
            if(product instanceof TicketTab) {
                tabs.add((TicketTab) product);
            }
        }
        return tabs;
    }

    public List<Ticket> getTickets() {
        List<Ticket> tickets = new ArrayList<Ticket>();
        for(Product product : products) {
            if(product instanceof Ticket) {
                tickets.add((Ticket) product);
            }
        }
        return tickets;
    }

    public List<Complaint> getComplaints() {
        return complaints;
    }

    public void setComplaints(List<Complaint> complaints) {
        this.complaints = complaints;
    }

    public void addBooking(Booking b)
    {
        this.bookings.add(b);
    }

    public void addProduct(Product b)
    {
        this.products.add(b);
    }

}
