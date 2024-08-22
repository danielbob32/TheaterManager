package il.cshaifasweng.OCSFMediatorExample.client.events;

import il.cshaifasweng.OCSFMediatorExample.entities.Movie;

public class MovieDeleteEvent {
    private final Movie movie;
    private final String movieType;

    public MovieDeleteEvent(Movie movie, String movieType) {
        this.movie = movie;
        this.movieType = movieType;
    }

    public Movie getMovie() {
        return movie;
    }

    public String getMovieType() {
        return movieType;
    }
}