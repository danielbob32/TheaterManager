package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.List;

@Entity
public class CinemaManager extends Worker{
 
    @OneToMany(mappedBy = "manager")
    private List<Cinema> cinemas;

    // Constructors
    public CinemaManager() {
    }

    public CinemaManager(String name, String password, int id, List<Cinema> cinemas) {
        super(name, password, id);
        this.cinemas = cinemas;
    }
    
    // Getters and setters
   
    public List<Cinema> getCinemas() {
        return cinemas;
    }

    public void setCinemas(List<Cinema> cinemas) {
        this.cinemas = cinemas;
    }
}