package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public abstract class Person {
    @Id
    private int person_id;
    private String name;
    private boolean isLoggedIn;

    public Person()
    {
    }

    public Person(String name, int person_id) {
        this.name = name;
        this.person_id = person_id;
        this.isLoggedIn = false;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return person_id;
    }

    public void setId(int id) {
        this.person_id = id;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

}
