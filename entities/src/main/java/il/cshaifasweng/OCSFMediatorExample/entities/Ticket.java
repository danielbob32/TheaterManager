package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Ticket extends Product {
    @ManyToOne
    private Movie movie;

    @OneToOne
    private Seat seat;

    @ManyToOne
    private MovieHall hall;

    // Constructors
    public Ticket() {
    }

    // Constructor for Ticket with super class Product
    public Ticket(int clientId, int price, boolean isActive, Cinema cinema, Movie movie, Seat seat, MovieHall hall, Date purchaseTime) {
        super(clientId, price, isActive, cinema, purchaseTime);
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
    
}
