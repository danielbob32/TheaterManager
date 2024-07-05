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

    // Constructors
    public Movie(String s, String titlesEnglish, String string, String movieActor, String s1, String showTime) {
    }

    public Movie() {
    }
    
    public Movie(String englishName, String hebrewName, String producer, String actors, int duration, String movieIcon, String synopsis, String genre, Date premier) {
        this.englishName = englishName;
        this.hebrewName = hebrewName;
        this.producer = producer;
        this.actors = actors;
        this.duration = duration;
        this.movieIcon = movieIcon;
        this.synopsis = synopsis;
        this.genre = genre;
        this.premier = premier;
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
}
