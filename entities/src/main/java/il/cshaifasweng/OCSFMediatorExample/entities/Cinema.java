package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.List;

@Entity
public class Cinema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String cinemaName;
    private String location;

    @OneToMany(mappedBy = "cinema")
    private List<MovieHall> movieHalls;

    @ManyToOne
    private CinemaManager manager;

    // Constructors
    public Cinema() {
    }

    public Cinema(String cinemaName, String location, List<MovieHall> movieHalls, CinemaManager manager) {
        this.cinemaName = cinemaName;
        this.location = location;
        this.movieHalls = movieHalls;
        this.manager = manager;
    }

    
    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<MovieHall> getMovieHalls() {
        return movieHalls;
    }

    public void setMovieHalls(List<MovieHall> movieHalls) {
        this.movieHalls = movieHalls;
    }

    public CinemaManager getManager() {
        return manager;
    }

    public void setManager(CinemaManager manager) {
        this.manager = manager;
    }
}
