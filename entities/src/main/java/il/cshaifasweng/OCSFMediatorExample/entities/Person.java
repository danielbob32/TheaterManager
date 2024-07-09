package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public abstract class Person {
    @Id
    private int personId;
    private String name;
    private boolean isLoggedIn;

    public Person()
    {
    }

    public Person(String name, int personId) {
        this.name = name;
        this.personId = personId;
        this.isLoggedIn = false;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int id) {
        this.personId = id;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

}
