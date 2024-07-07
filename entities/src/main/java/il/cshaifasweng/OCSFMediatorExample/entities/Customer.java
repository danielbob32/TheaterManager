package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class Customer {
    @Id
    private int customer_id;

    private String name;
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
        this.customer_id = id;
        this.name = name;
        this.email = email;
    }

    public Customer(int id) {
        this.customer_id = id;
    }

    // Getters and setters
    public int getId() {
        return customer_id;
    }

    public void setId(int id) {
        this.customer_id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    @JsonIgnore
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

    @JsonIgnore
    public List<TicketTab> getTicketTabs() {
        List<TicketTab> tabs = new ArrayList<TicketTab>();
        for(Product product : products) {
            if(product instanceof TicketTab) {
                tabs.add((TicketTab) product);
            }
        }
        return tabs;
    }
    @JsonIgnore
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

}
