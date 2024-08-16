package il.cshaifasweng.OCSFMediatorExample.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,  property = "personId")
public class Customer extends Person{

    private String email;

    @OneToMany(mappedBy = "customer")
    private List<Booking> bookings;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "customer_id")
    private List<Product> products;

    @OneToMany(mappedBy = "customer")
    private List<Complaint> complaints;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();


    // Constructors
    public Customer() {
    }

    public Customer(String name, String email, int id) {
        super(name, id);
        this.email = email;
        this.products = new ArrayList<Product>();
        this.bookings = new ArrayList<Booking>();
        this.complaints = new ArrayList<Complaint>();
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

    public void setProducts(List<Product> products) {
        this.products = products;
    }
    public List<Product> getProducts() {
        return products;
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
