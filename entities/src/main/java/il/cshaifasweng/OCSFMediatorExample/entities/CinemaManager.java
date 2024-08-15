package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.List;

@Entity
public class CinemaManager extends Worker{
 
    @OneToOne(mappedBy = "manager")
    private Cinema cinema;

    // Constructors
    public CinemaManager() {
        super();  // Calls the default constructor of Worker
        this.setWorkerType("CinemaManager");  // Ensures that the workerType is set correctly
    }


    // Parameterized constructor
    public CinemaManager(String name, String password, int id, Cinema cinema) {
        super(name, "CinemaManager", password, id);  // Setting the workerType explicitly
        this.cinema = cinema;
    }
    
    // Getters and setters
   


    public Cinema getCinema() {
        return this.cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }
}