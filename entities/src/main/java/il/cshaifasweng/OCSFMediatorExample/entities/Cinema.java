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
    private BranchManager manager;

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

    public BranchManager getManager() {
        return manager;
    }

    public void setManager(BranchManager manager) {
        this.manager = manager;
    }
}
