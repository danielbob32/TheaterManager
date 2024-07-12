package il.cshaifasweng.OCSFMediatorExample.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.List;

@Entity
public class MovieHall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int movieHall_id;

    private int hallNumber;

    @OneToMany(mappedBy = "movieHall")
    @JsonManagedReference
    private List<Seat> seats;

    @ManyToOne
    @JsonBackReference
    private Cinema cinema;

    // constructors
    public MovieHall() {
    }

    public MovieHall(int hallNumber, List<Seat> seats, Cinema cinema) {
        this.hallNumber = hallNumber;
        this.seats = seats;
        this.cinema = cinema;
    }

    // Getters and setters
    public int getMovieHall_id() {
        return movieHall_id;
    }

    public void setMovieHall_id(int id) {
        this.movieHall_id = id;
    }

    public int getHallNumber() {
        return hallNumber;
    }

    public void setHallNumber(int hallNumber) {
        this.hallNumber = hallNumber;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public Cinema getCinema() {
        return cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }
}
