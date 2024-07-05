package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.List;

@Entity
public class CinemaManager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String password;
    private boolean isLoggedIn;

    @OneToMany(mappedBy = "manager")
    private List<Cinema> cinemas;

    // Constructors
    public CinemaManager() {
    }

    public CinemaManager(String name, String password, boolean isLoggedIn, List<Cinema> cinemas) {
        this.name = name;
        this.password = password;
        this.isLoggedIn = isLoggedIn;
        this.cinemas = cinemas;
    }

    
    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public List<Cinema> getCinemas() {
        return cinemas;
    }

    public void setCinemas(List<Cinema> cinemas) {
        this.cinemas = cinemas;
    }
}