package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.List;


@Entity
public class Customer {
    @Id
    private int id;

    private String name;
    private String email;

    @OneToMany(mappedBy = "customer")
    private List<Booking> bookings;

    @OneToMany(mappedBy = "customer")
    private List<HomeMovieLink> homeMovies;

    @OneToMany(mappedBy = "customer")
    private List<TicketTab> ticketTabs;

    @OneToMany(mappedBy = "customer")
    private List<Complaint> complaints;

    // Constructors
    public Customer() {
    }

    public Customer(String name, String email, int id) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public List<HomeMovieLink> getHomeMovies() {
        return homeMovies;
    }

    public void setHomeMovies(List<HomeMovieLink> homeMovies) {
        this.homeMovies = homeMovies;
    }

    public List<TicketTab> getTicketTabs() {
        return ticketTabs;
    }

    public void setTicketTabs(List<TicketTab> ticketTabs) {
        this.ticketTabs = ticketTabs;
    }

    public List<Complaint> getComplaints() {
        return complaints;
    }

    public void setComplaints(List<Complaint> complaints) {
        this.complaints = complaints;
    }

}
