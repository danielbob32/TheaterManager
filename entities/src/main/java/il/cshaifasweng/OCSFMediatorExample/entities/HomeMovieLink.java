package il.cshaifasweng.OCSFMediatorExample.entities;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.util.Date;
@Entity
public class HomeMovieLink extends Product {
    private Date openTime;
    private Date closeTime;
    private boolean isOpen;
    private String watchLink = "www.Theater.link.";

    @ManyToOne
    @JoinColumn(name = "movie_id")
    @JsonBackReference(value = "movie-homeMovieLinks")
    private Movie movie;


    // Constructors
    public HomeMovieLink() {
        super();
    }

    public HomeMovieLink(Date openTime, Date closeTime, boolean isOpen, String watchLink, int clientId, int price, boolean isActive, Date purchaseTime) {
        super(clientId, price, isActive, purchaseTime);
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isOpen = isOpen;
        this.watchLink = watchLink;
    }


    // Getters and setters
    public Date getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Date openTime) {
        this.openTime = openTime;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }


    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getWatchLink() {
        return watchLink;
    }

    public void setWatchLink(String watchLink) {
        this.watchLink = watchLink;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public boolean getIsOpen() {
        return isOpen;
    }

    public static String generateRandomLink(String link) {
        return link + (int) (Math.random() * 1000);
    }
}
