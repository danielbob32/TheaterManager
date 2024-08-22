package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.Cinema;

import java.util.List;

public class CinemaListEvent {
    private List<Cinema> cinemas;

    public CinemaListEvent(List<Cinema> cinemas) {
        this.cinemas = cinemas;
    }

    public List<Cinema> getCinemas() {
        return cinemas;
    }
}