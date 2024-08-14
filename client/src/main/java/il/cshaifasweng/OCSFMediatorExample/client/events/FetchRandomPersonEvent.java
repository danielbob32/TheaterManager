package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.Person;

public class FetchRandomPersonEvent {
    private final Person person;

    public FetchRandomPersonEvent(Person person) {
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }
}
