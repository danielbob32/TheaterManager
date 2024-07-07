package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Worker {
    @Id
    private int worker_id;

    private String name;
    private String workerType;
    private String password;
    private boolean isLoggedIn;

    // Constructors
    public Worker() {
    }

    public Worker(String name, String password, int worker_id) {
        this.worker_id = worker_id;
        this.name = name;
        this.password = password;
    }

    public Worker(String name, String workerType, String password, int worker_id) {
        this.worker_id = worker_id;
        this.name = name;
        this.workerType = workerType;
        this.password = password;
    }

    public Worker(int id, String password) {
        this.worker_id = id;
        this.password = password;
        this.name = "";
        this.workerType = "";
        this.isLoggedIn = true;
    }


    // Getters and setters
    public int getWorker_id() {
        return worker_id;
    }

    public void setWorker_id(int id) {
        this.worker_id = id;
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


}
