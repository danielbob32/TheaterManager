package il.cshaifasweng.OCSFMediatorExample.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class,property="@cinema_id", scope = Cinema.class)
public class Cinema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cinema_id;

    private String cinemaName;
    private String location;

    @OneToMany(mappedBy = "cinema", fetch = FetchType.EAGER)
    private List<MovieHall> movieHalls;

    @OneToOne
    @JsonIgnore
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
    public int getCinema_id() {
        return cinema_id;
    }

    public void setCinema_id(int id) {
        this.cinema_id = id;
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
        return movieHalls != null ? movieHalls : new ArrayList<>();
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