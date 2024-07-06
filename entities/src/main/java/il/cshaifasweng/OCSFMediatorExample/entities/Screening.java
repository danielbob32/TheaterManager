package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Screening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int screening_id;

    @ManyToOne
    private Cinema cinema;

    @ManyToOne
    private MovieHall hall;

    private Date time;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "screening_id")
    private List<Seat> seats;

    private boolean isFull;

    // constructors
    public Screening() {
    }

    public Screening(Cinema cinema, MovieHall hall, Date time, List<Seat> seats, boolean isFull) {
        this.cinema = cinema;
        this.hall = hall;
        this.time = time;
        this.seats = seats;
        this.isFull = isFull;
    }
    

    // Getters and setters
    public int getScreening_id() {
        return screening_id;
    }

    public void setScreening_id(int id) {
        this.screening_id = id;
    }

    public Cinema getCinema() {
        return cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }

    public MovieHall getHall() {
        return hall;
    }

    public void setHall(MovieHall hall) {
        this.hall = hall;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public boolean isFull() {
        return isFull;
    }

    public void setFull(boolean full) {
        isFull = full;
    }
}
