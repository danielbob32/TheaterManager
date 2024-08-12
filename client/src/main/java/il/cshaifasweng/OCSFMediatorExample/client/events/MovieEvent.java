package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.Movie;

public class MovieEvent {
    private final Movie movie;

    public MovieEvent(Movie movie) {
        this.movie = movie;
    }

    public Movie getMovie() {
        return movie;
    }
}