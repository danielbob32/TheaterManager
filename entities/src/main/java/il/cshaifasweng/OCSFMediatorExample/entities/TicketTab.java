package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.*;

@Entity
public class TicketTab extends Product {

    private int amount;

//    @OneToMany
//    private List<Ticket> tickets;


//    @ManyToOne
//    private Movie movie;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "product_id")
    private List<Ticket> tickets;

    // Constructors
    public TicketTab() {
    }

    public TicketTab(Customer c, Date purchaseTime)
    {
        super(c.getPersonId(), 200, true, purchaseTime);
        this.amount = 20;
        tickets = new ArrayList<>(Arrays.asList(new Ticket[20]));
    }

    public TicketTab(Customer customer, Date purchaseTime, int price, Booking booking) {
        super(customer, price, purchaseTime, booking);
        this.amount = 20;
    }

    // Getters and setters
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(ArrayList<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

//    public Movie getMovie() {
//        return movie;
//    }
//
//    public void setMovie(Movie movie) {
//        this.movie = movie;
//    }
}
