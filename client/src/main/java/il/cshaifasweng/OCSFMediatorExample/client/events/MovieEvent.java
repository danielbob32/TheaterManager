package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.Movie;

public class MovieEvent {
    private final Movie movie;
    private final boolean success;

    public MovieEvent(Movie movie, boolean success) {
        this.movie = movie;
        this.success = success;
    }

    public Movie getMovie() {
        return movie;
    }

    public boolean getSuccess() {
        return success;
    }
}