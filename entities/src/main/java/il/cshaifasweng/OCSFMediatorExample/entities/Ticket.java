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

    @ManyToOne
    private Screening screening;

    // Constructors
    public Ticket() {
    }

    // Constructor for Ticket with super class Product
    public Ticket(int clientId, int price, boolean isActive, Cinema cinema, Movie movie, Seat seat, MovieHall hall, Date purchaseTime) {
        super(clientId, price, isActive, purchaseTime);
        this.movie = movie;
        this.seat = seat;
        this.hall = hall;
    }

    // Constructor for Ticket with super class Product
    public Ticket(int clientId, int price, boolean isActive, Cinema cinema, Movie movie, Seat seat, MovieHall hall, Date purchaseTime, Screening screening) {
        super(clientId, price, isActive, purchaseTime);
        this.movie = movie;
        this.seat = seat;
        this.hall = hall;
        this.screening = screening;
    }

    public Ticket(int clientId, int price, Date purchaseTime, Screening screening, Seat seat) {
        super(clientId, price, true, purchaseTime);
        this.movie = screening.getMovie();
        this.seat = seat;
        this.hall = screening.getHall();
        this.screening = screening;
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

    public Screening getScreening() {
        return screening;
    }

    public void setScreening(Screening screening) {
        this.screening = screening;
    }
}
