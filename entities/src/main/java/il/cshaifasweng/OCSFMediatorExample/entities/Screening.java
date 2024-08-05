package il.cshaifasweng.OCSFMediatorExample.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "screening_id")
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Screening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int screening_id;

    @ManyToOne
    private Cinema cinema;

//    @ManyToOne
//    @JoinColumn(name = "movie_id")
//    @JsonBackReference(value = "movie-screenings")
//    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    @JsonIgnoreProperties("screenings")
    private Movie movie;

    @ManyToOne
    private MovieHall hall;

    private Date time;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "screening_id")
    private List<Seat> seats;

//    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
//    @JoinColumn(name = "screening_id")
//    private List<Ticket> tickets;

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
//        this.tickets = new ArrayList<Ticket>();
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
            //System.out.println("Adding screening to movie on the screening entity");
            movie.getScreenings().add(this);
        }
    }

//    public void setTickets(List<Ticket> tickets)
//    {
//        this.tickets=tickets;
//    }
//
//    public List<Ticket> getTickets()
//    {
//        return this.tickets;
//    }

}
