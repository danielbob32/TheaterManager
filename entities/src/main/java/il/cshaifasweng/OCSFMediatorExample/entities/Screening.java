package il.cshaifasweng.OCSFMediatorExample.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    @JoinColumn(name = "movie_id")
    @JsonIgnoreProperties("screenings")
    private Movie movie;

    @ManyToOne(fetch = FetchType.EAGER)
    private MovieHall hall;

    private Date time;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "screening_id")
    private List<Seat> seats;

    private boolean isFull;

    // constructors
    public Screening() {
    }

    public Screening(Cinema cinema, MovieHall hall, Movie movie, Date time, List<Seat> seats, boolean isFull) {
        this.cinema = cinema;
        this.hall = hall;
        this.movie = movie;
        this.time = time;
        this.seats = seats;
        this.isFull = isFull;
        setMovie(movie);
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

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
        if (movie != null && !movie.getScreenings().contains(this)) {
            movie.getScreenings().add(this);
        }
    }

}
