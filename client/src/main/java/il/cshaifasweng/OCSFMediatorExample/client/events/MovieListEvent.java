package il.cshaifasweng.OCSFMediatorExample.client.events;
import il.cshaifasweng.OCSFMediatorExample.entities.*;

import java.util.List;

public class MovieListEvent {
    private final List<Movie> movies;
    String moviesString;

    public MovieListEvent(List<Movie> movies) {
        this.movies = movies;
    }
    public MovieListEvent(String movies) {
        this.movies = null;
        this.moviesString = movies;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public String getMoviesString() {
        return moviesString;
    }
}