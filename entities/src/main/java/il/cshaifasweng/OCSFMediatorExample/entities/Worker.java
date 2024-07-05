package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;

@Entity
public class Worker {
    @Id
    private int id;

    private String name;
    private String workerType;
    private String password;
    private boolean isLoggedIn;

    @ManyToOne
    private Cinema cinema;

    // Constructors
    public Worker() {
    }

    public Worker(String name, String password, int id) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public Worker(String name, String workerType, String password, int id) {
        this.id = id;
        this.name = name;
        this.workerType = workerType;
        this.password = password;
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

    public String getWorkerType() {
        return workerType;
    }

    public void setWorkerType(String workerType) {
        this.workerType = workerType;
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

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public Cinema getCinema() {
        return cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }


}
