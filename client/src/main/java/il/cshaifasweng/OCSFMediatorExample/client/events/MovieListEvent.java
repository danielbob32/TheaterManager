package il.cshaifasweng.OCSFMediatorExample.client.events;
import il.cshaifasweng.OCSFMediatorExample.entities.*;

import java.util.List;

public class MovieListEvent {
    private final List<Movie> movies;

    public MovieListEvent(List<Movie> movies) {
        this.movies = movies;
    }

    public List<Movie> getMovies() {
        return movies;
    }
}