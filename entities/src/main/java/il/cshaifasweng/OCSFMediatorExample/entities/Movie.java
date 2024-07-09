package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String englishName;
    private String hebrewName;
    private String producer;
    private String actors;
    private int duration;
    private String movieIcon; // Assuming this is a URL or path to the image
    private String synopsis;
    private String genre;
    private Date premier;
    private boolean isHome; // True if it's possible to buy links for this cinema
    private boolean isCinema;  // True if it's possible to buy tickets for this cinema

    // Constructors
    public Movie() {
    }
    
    public Movie(String englishName, String hebrewName, String producer, String actors, int duration, String movieIcon,
                 String synopsis, String genre, Date premier, boolean ishome, boolean iscinema) {
        this.englishName = englishName;
        this.hebrewName = hebrewName;
        this.producer = producer;
        this.actors = actors;
        this.duration = duration;
        this.movieIcon = movieIcon;
        this.synopsis = synopsis;
        this.genre = genre;
        this.premier = premier;
        this.isHome = ishome;
        this.isCinema = iscinema;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getHebrewName() {
        return hebrewName;
    }

    public void setHebrewName(String hebrewName) {
        this.hebrewName = hebrewName;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getMovieIcon() {
        return movieIcon;
    }

    public void setMovieIcon(String movieIcon) {
        this.movieIcon = movieIcon;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Date getPremier() {
        return premier;
    }

    public void setPremier(Date premier) {
        this.premier = premier;
    }

    public void setIsHome(boolean isHome)
    {
        this.isHome = isHome;
    }
    public boolean getIsHome()
    {
        return isHome;
    }

    public boolean getIsCinema()
    {
        return isCinema;
    }

    public void setIsCinema(boolean isCinema)
    {
        this.isCinema = isCinema;
    }
}
