package il.cshaifasweng.OCSFMediatorExample.client.events;

import java.util.List;

public class CinemaListEvent {
    private List<String> cinemas;

    public CinemaListEvent(List<String> cinemas) {
        this.cinemas = cinemas;
    }

    public List<String> getCinemas() {
        return cinemas;
    }
}