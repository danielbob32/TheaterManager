package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import java.sql.Date;
import java.util.List;

@Entity
public class TicketTab extends Product {
    private int amount;

    @OneToMany
    private List<Ticket> tickets;

    @ManyToOne
    private Movie movie;

    // Constructors
    public TicketTab() {
    }

    public TicketTab(int amount, List<Ticket> tickets, Movie movie, int clientId, int price, boolean isActive, Cinema cinema, Date purchaseTime) {
        super(clientId, price, isActive, cinema, purchaseTime);
        this.amount = amount;
        this.tickets = tickets;
        this.movie = movie;
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

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
}
