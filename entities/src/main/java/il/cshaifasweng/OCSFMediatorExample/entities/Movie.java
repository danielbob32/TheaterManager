package il.cshaifasweng.OCSFMediatorExample.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import javax.persistence.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

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
    @Lob
    private byte[] movieIcon;
    private String synopsis;
    private Date premier;
    private boolean isHome; // True if it's possible to buy links for this cinema
    private boolean isCinema;  // True if it's possible to buy tickets for this cinema
    private int cinemaPrice;
    private int homePrice;
    @Transient
    private String movieIconAsString;

    @ElementCollection
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre")
    private List<String> genres = new ArrayList<>();


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "movie", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("movie")
    private List<Screening> screenings = new ArrayList<>();

    // Constructors
    public Movie() {
    }


    public Movie(String englishName, String hebrewName, String producer, String actors, int duration, byte[] movieIcon,
                 String synopsis, List<String> genres, Date premier, boolean isHome, boolean isCinema) {
        this.englishName = englishName;
        this.hebrewName = hebrewName;
        this.producer = producer;
        this.actors = actors;
        this.duration = duration;
        this.movieIcon = movieIcon;
        this.synopsis = synopsis;
        this.genres = genres;
        this.premier = premier;
        this.isHome = isHome;
        this.isCinema = isCinema;
        this.cinemaPrice = 0;
        this.homePrice = 0;
        this.screenings = new ArrayList<>();
    }

    public Movie(String englishName, String hebrewName, String producer, String actors, int duration, byte[] movieIcon, String synopsis, List<String> genres, Date premier, boolean isHome, boolean isCinema, int cinemaPrice, int homePrice) {
        this.englishName = englishName;
        this.hebrewName = hebrewName;
        this.producer = producer;
        this.actors = actors;
        this.duration = duration;
        this.movieIcon = movieIcon;
        this.synopsis = synopsis;
        this.genres = genres;
        this.premier = premier;
        this.isHome = isHome;
        this.isCinema = isCinema;
        this.cinemaPrice = cinemaPrice;
        this.homePrice = homePrice;
        this.screenings = new ArrayList<>();
    }


    private Blob createBlob(byte[] bytes) {
        try {
            return new javax.sql.rowset.serial.SerialBlob(bytes);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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

    public byte[] getMovieIcon() {
        return movieIcon;
    }

    public String getMovieIconAsString() {
        if (movieIcon == null) {
            System.out.println("movieIcon is null");
            return null;
        }
        // Convert byte[] to Base64 string
        String base64 = Base64.getEncoder().encodeToString(movieIcon);
        System.out.println("Base64 string length: " + base64.length());
        return base64;
    }

    @JsonSetter("movieIconAsString")
    public void setMovieIconAsString(String movieIconAsString) {
        this.movieIconAsString = movieIconAsString;
        if (movieIconAsString != null) {
            // Convert Base64 string back to byte array
            this.movieIcon = Base64.getDecoder().decode(movieIconAsString);
        }
    }

    public void setMovieIcon(byte[] movieIcon) {
        this.movieIcon = movieIcon;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
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
        if (!this.screenings.contains(screening)) {
            this.screenings.add(screening);
            screening.setMovie(this);
        }
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

    @Override
    public String toString() {
        return this.getEnglishName();
    }

    public Movie fromString (String movieName) {
        return this;
    }

}
