package il.cshaifasweng.OCSFMediatorExample.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@Entity
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int seat_id;

    private int seatNumber;
    private int seatRow;
    private boolean isAvailable;

    @ManyToOne
    @JsonBackReference
    private MovieHall movieHall;

    // Constructors
    public Seat() {
    }

    public Seat(int seatNumber, int seatRow, boolean isAvailable, MovieHall movieHall) {
        this.seatNumber = seatNumber;
        this.seatRow = seatRow;
        this.isAvailable = isAvailable;
        this.movieHall = movieHall;
    }

    // Getters and setters
    public int getSeat_id() {
        return seat_id;
    }

    public void setSeat_id(int id) {
        this.seat_id = id;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(int seatRow) {
        this.seatRow = seatRow;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public MovieHall getMovieHall() {
        return movieHall;
    }

    public void setMovieHall(MovieHall movieHall) {
        this.movieHall = movieHall;
    }
}
