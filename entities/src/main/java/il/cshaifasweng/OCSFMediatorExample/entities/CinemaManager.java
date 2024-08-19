package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

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


    public static CinemaManager fromWorker(Worker worker) {
        if (!"CinemaManager".equals(worker.getWorkerType())) {
            throw new IllegalArgumentException("Worker type must be CinemaManager");
        }

        CinemaManager cinemaManager = new CinemaManager();
        cinemaManager.setName(worker.getName());
        cinemaManager.setPersonId(worker.getPersonId());
        cinemaManager.setPassword(worker.getPassword());
        cinemaManager.setWorkerType("CinemaManager");


        return cinemaManager;
    }
    // Getters and setters
   


    public Cinema getCinema() {
        return this.cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }
}