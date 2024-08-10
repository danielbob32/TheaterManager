package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.List;

@Entity
public class CinemaManager extends Worker{
 
    @OneToOne(mappedBy = "manager")
    private Cinema cinema;

    // Constructors
    public CinemaManager() {
    }


    public CinemaManager(String name, String password, int id, Cinema cinema) {
        super(name, password, id);
        this.cinema = cinema;
    }
    
    // Getters and setters
   
    public Cinema getCinemas() {
        return this.cinema;
    
    }

    public Cinema getCinema() {
        return this.cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }
}