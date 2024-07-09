package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity

public class Worker extends Person{

    private String name;
    private String workerType;
    private String password;

    // Constructors
    public Worker() {
    }

    public Worker(String name, String password, int worker_id) {
        super(name, worker_id);
        this.password = password;
    }

    public Worker(String name, String workerType, String password, int worker_id) {
        super(name, worker_id);
        this.name = name;
        this.workerType = workerType;
        this.password = password;
    }

    public Worker(int id, String password) {
        this.worker_id = id;
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



    public int getId() {
        return worker_id;
    }
}
