package il.cshaifasweng.OCSFMediatorExample.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
    private int cinemaPrice;
    private int homePrice;

//    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "movie-screenings")
    @JoinTable(
            name = "movie_screenings",
            joinColumns = @JoinColumn(name = "id"),
            inverseJoinColumns = @JoinColumn(name = "screening_id")
    )
    private List<Screening> screenings = new ArrayList<>();


    // Constructors
    public Movie() {
    }

    public Movie(String englishName, String hebrewName, String producer, String actors, int duration, String movieIcon,
                 String synopsis, String genre, Date premier, boolean isHome, boolean isCinema, int cinemaPrice, int homePrice) {
        this.englishName = englishName;
        this.hebrewName = hebrewName;
        this.producer = producer;
        this.actors = actors;
        this.duration = duration;
        this.movieIcon = movieIcon;
        this.synopsis = synopsis;
        this.genre = genre;
        this.premier = premier;
        this.isHome = isHome;
        this.isCinema = isCinema;
        this.cinemaPrice = cinemaPrice;
        this.homePrice = homePrice;
        this.screenings = new ArrayList<>();
//        this.cinemas = new ArrayList<>();
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
        this.cinemaPrice = 0;
        this.homePrice = 0;
        this.screenings = new ArrayList<>();
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

    public int getHomePrice() {
        return homePrice;
    }

    public int getCinemaPrice() {
        return cinemaPrice;
    }

    public void setCinemaPrice(int cinemaPrice) {
        this.cinemaPrice = cinemaPrice;
    }
    public void setHomePrice(int homePrice) {
        this.homePrice = homePrice;
    }

    // New method to add a screening
    public void addScreening(Screening screening) {
        screenings.add(screening);
    }

    // New method to remove a screening
    public void removeScreening(Screening screening) {
        screenings.remove(screening);
        screening.setMovie(null);
    }

    // Getter and setter for screenings
    public List<Screening> getScreenings() {
        return screenings;
    }

    public void setScreenings(List<Screening> screenings) {
        this.screenings = screenings;
    }

}
