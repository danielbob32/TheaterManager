package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;

@Entity
public class Ticket extends Product {
    @ManyToOne
    private Movie movie;

    @ManyToOne
    private Seat seat;

    @ManyToOne
    private MovieHall hall;

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
