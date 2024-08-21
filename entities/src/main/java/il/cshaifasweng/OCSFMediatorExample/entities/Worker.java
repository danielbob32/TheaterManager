package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;

@Entity

public class Worker extends Person{

    private String workerType;
    private String password;

    // Constructors
    public Worker() {
    }

    public Worker(String name, String password, int worker_id) {
        super(name, worker_id);
        this.password = password;
        this.workerType = "Content";
    }

    public Worker(String name, String workerType, String password, int worker_id) {
        super(name, worker_id);
        this.workerType = workerType;
        this.password = password;
    }

    // Getters and setters

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

}